package com.cloudBasedStorageService.StorageContainer.service;

import com.cloudBasedStorageService.StorageContainer.model.File;
import com.cloudBasedStorageService.StorageContainer.model.Folder;
import com.cloudBasedStorageService.StorageContainer.model.ItemType;
import com.cloudBasedStorageService.StorageContainer.model.ShareLink;
import com.cloudBasedStorageService.StorageContainer.model.User;
import com.cloudBasedStorageService.StorageContainer.model.dto.FileDownloadResponse;
import com.cloudBasedStorageService.StorageContainer.model.dto.PublicShareView;
import com.cloudBasedStorageService.StorageContainer.model.dto.ShareLinkResponse;
import com.cloudBasedStorageService.StorageContainer.repo.FileRepository;
import com.cloudBasedStorageService.StorageContainer.repo.FolderRepository;
import com.cloudBasedStorageService.StorageContainer.repo.ShareLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShareLinkService {

    /**
     * 128 bits, rendered as 22 base64url characters. Enough entropy that the token
     * cannot be guessed or brute-forced, which is the only thing standing between an
     * unauthenticated visitor and the file.
     */
    private static final int TOKEN_BYTES = 16;

    private static final SecureRandom RANDOM = new SecureRandom();

    private final ShareLinkRepository shareLinkRepository;
    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final FileStorageService fileStorageService;

    // ---------- authenticated management ----------

    /**
     * Idempotent, like Drive's own toggle: asking for a link on an item that already
     * has one returns the existing token instead of minting a second live URL.
     */
    public ShareLinkResponse create(Integer itemId, ItemType itemType, User caller) {

        requireOwnedItem(itemType, itemId, caller);

        ShareLink link = shareLinkRepository
                .findByItemIdAndItemType(itemId, itemType)
                .orElseGet(() -> {

                    ShareLink created = new ShareLink();

                    created.setItemId(itemId);
                    created.setItemType(itemType);
                    created.setCreatedBy(caller);
                    created.setToken(newToken());

                    return shareLinkRepository.save(created);
                });

        return toResponse(link);
    }

    public ShareLinkResponse find(Integer itemId, ItemType itemType, User caller) {

        requireOwnedItem(itemType, itemId, caller);

        return shareLinkRepository
                .findByItemIdAndItemType(itemId, itemType)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "This item has no share link"));
    }

    public void revoke(Integer linkId, User caller) {

        ShareLink link = shareLinkRepository.findById(linkId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Share link not found"));

        if (!link.getCreatedBy().getUserId().equals(caller.getUserId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You are not allowed to revoke this link");
        }

        shareLinkRepository.delete(link);
    }

    /**
     * Called from the delete paths so removing an item cannot leave a live public
     * credential behind.
     */
    public void revokeForItem(ItemType itemType, Integer itemId) {

        shareLinkRepository.findByItemIdAndItemType(itemId, itemType)
                .ifPresent(shareLinkRepository::delete);
    }

    // ---------- anonymous reads ----------

    public PublicShareView view(String token) {

        ShareLink link = requireLink(token);

        if (link.getItemType() == ItemType.FILE) {
            return fileView(link, requireFile(link.getItemId()));
        }

        return folderView(link, requireFolder(link.getItemId()));
    }

    /**
     * Lets a folder link browse into its subfolders without widening the grant: the
     * requested folder has to be the linked folder or one of its descendants.
     */
    public PublicShareView viewSubfolder(String token, Integer folderId) {

        ShareLink link = requireLink(token);

        if (link.getItemType() != ItemType.FOLDER) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "This share link points at a file");
        }

        Folder folder = requireFolder(folderId);

        if (!folderId.equals(link.getItemId())
                && !isDescendantOfFolder(folder, link.getItemId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "This folder is not part of the shared link");
        }

        return folderView(link, folder);
    }

    public FileDownloadResponse resolveFile(String token, Integer fileId) {

        File file = requireLinkedFile(token, fileId);

        return new FileDownloadResponse(
                file.getFileName(),
                file.getFileType(),
                fileStorageService.resolve(file.getFilePath())
        );
    }

    /**
     * The shared blob endpoint takes both a token and a file id, so this is the check
     * that stops one valid link from being used to stream any other file in the
     * database by walking ids.
     */
    public File requireLinkedFile(String token, Integer fileId) {

        ShareLink link = requireLink(token);

        File file = requireFile(fileId);

        if (link.getItemType() == ItemType.FILE) {

            if (!link.getItemId().equals(file.getId())) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "This file is not part of the shared link");
            }

            return file;
        }

        if (file.getFolder() == null || !isDescendantOfFolder(file.getFolder(), link.getItemId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "This file is not part of the shared link");
        }

        return file;
    }

    // ---------- internals ----------

    private ShareLink requireLink(String token) {

        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Share link not found");
        }

        return shareLinkRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Share link not found or has been revoked"));
    }

    private PublicShareView fileView(ShareLink link, File file) {

        return new PublicShareView(
                ItemType.FILE.name(),
                file.getId(),
                file.getFileName(),
                link.getCreatedBy().getUserEmail(),
                List.of(),
                List.of(toItem(file))
        );
    }

    private PublicShareView folderView(ShareLink link, Folder folder) {

        List<PublicShareView.Item> childFolders = folderRepository
                .findByParentFolder(folder)
                .stream()
                .map(child -> new PublicShareView.Item(
                        child.getId(),
                        child.getFolderName(),
                        null,
                        null))
                .toList();

        List<PublicShareView.Item> childFiles = fileRepository
                .findByFolder(folder)
                .stream()
                .map(this::toItem)
                .toList();

        return new PublicShareView(
                ItemType.FOLDER.name(),
                folder.getId(),
                folder.getFolderName(),
                link.getCreatedBy().getUserEmail(),
                childFolders,
                childFiles
        );
    }

    private PublicShareView.Item toItem(File file) {

        return new PublicShareView.Item(
                file.getId(),
                file.getFileName(),
                file.getFileType(),
                file.getFileSize()
        );
    }

    /**
     * Walks up the parent chain rather than the whole subtree, so the cost is the
     * folder depth instead of every descendant id.
     */
    private boolean isDescendantOfFolder(Folder start, Integer ancestorId) {

        Folder current = start;

        while (current != null) {

            if (current.getId().equals(ancestorId)) {
                return true;
            }

            current = current.getParentFolder();
        }

        return false;
    }

    private void requireOwnedItem(ItemType itemType, Integer itemId, User caller) {

        Integer ownerId = itemType == ItemType.FILE
                ? requireFile(itemId).getCreatedBy().getUserId()
                : requireFolder(itemId).getCreatedBy().getUserId();

        if (!ownerId.equals(caller.getUserId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the owner can create or manage a share link for this "
                            + (itemType == ItemType.FILE ? "file" : "folder"));
        }
    }

    private File requireFile(Integer fileId) {

        return fileRepository.findById(fileId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "File not found"));
    }

    private Folder requireFolder(Integer folderId) {

        return folderRepository.findById(folderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Folder not found"));
    }

    private ShareLinkResponse toResponse(ShareLink link) {

        String name = link.getItemType() == ItemType.FILE
                ? requireFile(link.getItemId()).getFileName()
                : requireFolder(link.getItemId()).getFolderName();

        return new ShareLinkResponse(
                link.getId(),
                link.getToken(),
                link.getItemId(),
                link.getItemType(),
                name,
                link.getCreatedBy().getUserEmail(),
                link.getCreatedAt()
        );
    }

    private String newToken() {

        byte[] bytes = new byte[TOKEN_BYTES];

        RANDOM.nextBytes(bytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
