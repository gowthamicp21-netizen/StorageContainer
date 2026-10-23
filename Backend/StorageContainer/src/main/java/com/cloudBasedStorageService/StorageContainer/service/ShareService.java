package com.cloudBasedStorageService.StorageContainer.service;

import com.cloudBasedStorageService.StorageContainer.model.*;
import com.cloudBasedStorageService.StorageContainer.model.dto.ShareRequest;
import com.cloudBasedStorageService.StorageContainer.model.dto.ShareResponse;
import com.cloudBasedStorageService.StorageContainer.model.dto.SharedFolderResponse;
import com.cloudBasedStorageService.StorageContainer.repo.FileRepository;
import com.cloudBasedStorageService.StorageContainer.repo.FolderRepository;
import com.cloudBasedStorageService.StorageContainer.repo.ShareRepository;
import com.cloudBasedStorageService.StorageContainer.repo.UserRepository;
import com.cloudBasedStorageService.StorageContainer.util.Emails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShareService {

    private final ShareRepository shareRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;

    public Share createShare(ShareRequest request) {

        User loggedInUser = getLoggedInUser();

        String recipient = Emails.normalize(request.email());

        if (recipient.equalsIgnoreCase(loggedInUser.getUserEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "You cannot share with yourself");
        }

        if (request.permission() != Role.VIEWER && request.permission() != Role.EDITOR) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Permission must be VIEWER or EDITOR");
        }

        if (userRepository.findByUserEmail(recipient).isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No StorageContainer account uses that email address");
        }

        requireOwnership(request.itemType(), request.itemId(), loggedInUser);

        if (shareRepository
                .findByEmailAndItemIdAndItemType(recipient, request.itemId(), request.itemType())
                .isPresent()) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "That item is already shared with this person");
        }

        Share share = new Share();

        share.setEmail(recipient);
        share.setItemId(request.itemId());
        share.setItemType(request.itemType());
        share.setPermission(request.permission());
        share.setSharedBy(loggedInUser);

        return shareRepository.save(share);
    }

    public List<ShareResponse> findSharedWithMe() {

        User user = getLoggedInUser();

        List<Share> shares =
                shareRepository.findAllByEmail(Emails.normalize(user.getUserEmail()));

        List<ShareResponse> response = new ArrayList<>();

        for (Share share : shares) {

            if (share.getItemType().equals(ItemType.FILE)) {

                fileRepository.findById(share.getItemId())
                        .ifPresent(file -> response.add(new ShareResponse(
                                share.getId(),
                                share.getEmail(),
                                share.getItemId(),
                                share.getItemType(),
                                share.getPermission(),
                                file.getFileName(),
                                share.getSharedBy().getUserEmail())));

            } else {

                folderRepository.findById(share.getItemId())
                        .ifPresent(folder -> response.add(new ShareResponse(
                                share.getId(),
                                share.getEmail(),
                                share.getItemId(),
                                share.getItemType(),
                                share.getPermission(),
                                folder.getFolderName(),
                                share.getSharedBy().getUserEmail())));
            }
        }

        return response;
    }

    public void deleteShare(Integer id) {

        User caller = getLoggedInUser();

        Share share = shareRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Share not found"));

        boolean isSharer = share.getSharedBy().getUserId().equals(caller.getUserId());

        boolean isItemOwner = isItemOwner(share.getItemType(), share.getItemId(), caller);

        if (!isSharer && !isItemOwner) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to remove this share");
        }

        shareRepository.delete(share);
    }

    public SharedFolderResponse getSharedFolder(Integer folderId) {

        User user = getLoggedInUser();

        if (!hasFolderAccess(folderId, user.getUserEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not have access to this folder"
            );
        }

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Folder not found"
                ));

        List<Folder> folders = folderRepository.findByParentFolder(folder);

        List<File> files = fileRepository.findByFolder(folder);

        return new SharedFolderResponse(folder, folders, files);
    }

    public boolean hasFolderAccess(Integer folderId, String userEmail) {

        String email = Emails.normalize(userEmail);

        Folder folder = folderRepository.findById(folderId).orElse(null);

        while (folder != null) {

            boolean shared = shareRepository
                    .findByEmailAndItemIdAndItemType(email, folder.getId(), ItemType.FOLDER)
                    .isPresent();

            if (shared) {
                return true;
            }

            folder = folder.getParentFolder();
        }

        return false;
    }

    private void requireOwnership(ItemType itemType, Integer itemId, User caller) {

        if (itemType == ItemType.FILE) {

            File file = fileRepository.findById(itemId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "File not found"));

            if (!file.getCreatedBy().getUserId().equals(caller.getUserId())) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "You are not allowed to share this file");
            }

        } else {

            Folder folder = folderRepository.findById(itemId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Folder not found"));

            if (!folder.getCreatedBy().getUserId().equals(caller.getUserId())) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "You are not allowed to share this folder");
            }
        }
    }

    private boolean isItemOwner(ItemType itemType, Integer itemId, User caller) {

        Integer ownerId = itemType == ItemType.FILE
                ? fileRepository.findById(itemId).map(File::getCreatedBy).map(User::getUserId).orElse(null)
                : folderRepository.findById(itemId).map(Folder::getCreatedBy).map(User::getUserId).orElse(null);

        return caller.getUserId().equals(ownerId);
    }

    private User getLoggedInUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        return userService.getUserByUsername(authentication.getName());
    }
}
