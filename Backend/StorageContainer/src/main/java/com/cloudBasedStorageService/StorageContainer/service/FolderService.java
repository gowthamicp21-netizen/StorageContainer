package com.cloudBasedStorageService.StorageContainer.service;

import com.cloudBasedStorageService.StorageContainer.model.File;
import com.cloudBasedStorageService.StorageContainer.model.Folder;
import com.cloudBasedStorageService.StorageContainer.model.ItemType;
import com.cloudBasedStorageService.StorageContainer.model.User;
import com.cloudBasedStorageService.StorageContainer.model.dto.CreateFolderRequest;
import com.cloudBasedStorageService.StorageContainer.repo.FileRepository;
import com.cloudBasedStorageService.StorageContainer.repo.FolderRepository;
import com.cloudBasedStorageService.StorageContainer.repo.UserRepository;
import com.cloudBasedStorageService.StorageContainer.util.Emails;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class FolderService {

    private final FolderRepository folderRepository;
    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final ShareLinkService shareLinkService;

    public FolderService(
            FolderRepository folderRepository,
            UserRepository userRepository,
            FileRepository fileRepository,
            ShareLinkService shareLinkService) {

        this.folderRepository = folderRepository;
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
        this.shareLinkService = shareLinkService;
    }

    public List<Folder> getRootFolders() {

        return folderRepository.findByCreatedByAndParentFolderIsNull(currentUser());
    }

    public Folder saveFolder(CreateFolderRequest request) {

        User user = currentUser();

        Folder folder = new Folder();

        folder.setFolderName(request.folderName().trim());
        folder.setCreatedBy(user);

        if (request.parentFolderId() != null) {
            folder.setParentFolder(requireOwnedFolder(request.parentFolderId(), user));
        }

        return folderRepository.save(folder);
    }

    public List<Folder> getChildFolders(Integer parentFolderId) {

        User user = currentUser();

        Folder parentFolder = requireOwnedFolder(parentFolderId, user);

        return folderRepository.findByCreatedByAndParentFolder(user, parentFolder);
    }

    /**
     * Entry point is transactional so a failure partway through the tree cannot
     * leave a half-deleted folder hierarchy behind. The returned storage keys are
     * deleted from disk by the caller once this transaction commits, so an aborted
     * delete never unlinks blobs that rows still point at.
     */
    @Transactional
    public List<String> deleteFolder(Integer folderId, User user) {

        List<String> orphanedKeys = new ArrayList<>();

        deleteFolderRecursively(requireOwnedFolder(folderId, user), orphanedKeys);

        return orphanedKeys;
    }

    public Folder renameFolder(Integer folderId, String newName, User user) {

        Folder folder = requireOwnedFolder(folderId, user);

        if (newName == null || newName.trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Folder name cannot be empty");
        }

        folder.setFolderName(newName.trim());

        return folderRepository.save(folder);
    }

    private void deleteFolderRecursively(Folder folder, List<String> orphanedKeys) {

        List<File> files = fileRepository.findByFolderId(folder.getId());

        if (!files.isEmpty()) {
            files.forEach(f -> {
                orphanedKeys.add(f.getFilePath());
                shareLinkService.revokeForItem(ItemType.FILE, f.getId());
            });
            fileRepository.deleteAll(files);
        }

        for (Folder subFolder : folderRepository.findByParentFolder(folder)) {
            deleteFolderRecursively(subFolder, orphanedKeys);
        }

        shareLinkService.revokeForItem(ItemType.FOLDER, folder.getId());

        folderRepository.delete(folder);
    }

    private Folder requireOwnedFolder(Integer folderId, User user) {

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Folder not found"));

        if (!folder.getCreatedBy().getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You do not have access to this folder");
        }

        return folder;
    }

    private User currentUser() {

        String userEmail = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByUserEmail(Emails.normalize(userEmail))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
    }
}
