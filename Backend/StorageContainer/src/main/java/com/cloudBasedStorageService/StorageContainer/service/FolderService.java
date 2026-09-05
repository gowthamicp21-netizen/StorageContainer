package com.cloudBasedStorageService.StorageContainer.service;

import com.cloudBasedStorageService.StorageContainer.model.File;
import com.cloudBasedStorageService.StorageContainer.model.Folder;
import com.cloudBasedStorageService.StorageContainer.model.User;
import com.cloudBasedStorageService.StorageContainer.repo.FileRepository;
import com.cloudBasedStorageService.StorageContainer.repo.FolderRepository;
import com.cloudBasedStorageService.StorageContainer.repo.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FolderService {

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileRepository fileRepository;

    public List<Folder> getRootFolders() {

        String userEmail = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository
                .findByUserEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return folderRepository.findByCreatedByAndParentFolderIsNull(user);
    }

    public Folder saveFolder(Folder folder) {

        String userEmail = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository
                .findByUserEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        folder.setCreatedBy(user);

        return folderRepository.save(folder);
    }

    public List<Folder> getChildFolders(Integer parentFolderId) {

        String userEmail = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository
                .findByUserEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Folder parentFolder=folderRepository.findById(parentFolderId).get();

        return folderRepository.findByCreatedByAndParentFolder(
                user,
                parentFolder
        );
    }

    public void deleteFolder(Integer folderId) {
        Folder folder=folderRepository.findById(folderId).orElseThrow(() ->
                new RuntimeException("Folder not found with id: " + folderId)
        );
        deleteFolderRecursively(folder);
    }
    @Transactional
    private void deleteFolderRecursively(Folder folder){

        List<File> files = fileRepository.findByFolderId(folder.getId());

        if (files != null && !files.isEmpty()) {

            fileRepository.deleteAll(files);
        }

        List<Folder> subFolders =
                folderRepository.findByParentFolder(folder);

        for (Folder subFolder : subFolders) {

            deleteFolderRecursively(subFolder);
        }
        folderRepository.delete(folder);

    }
    public Folder renameFolder(
            Integer folderId,
            String newName,
            User user) {

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() ->
                        new RuntimeException("Folder not found"));

        if (!folder.getCreatedBy()
                .getUserId()
                .equals(user.getUserId())) {

            throw new RuntimeException(
                    "You do not have access to this folder");
        }

        if (newName == null || newName.trim().isEmpty()) {
            throw new RuntimeException(
                    "Folder name cannot be empty");
        }

        folder.setFolderName(newName.trim());

        return folderRepository.save(folder);
    }

}
