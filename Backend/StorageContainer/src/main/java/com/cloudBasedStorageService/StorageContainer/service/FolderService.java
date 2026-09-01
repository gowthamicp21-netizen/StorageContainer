package com.cloudBasedStorageService.StorageContainer.service;

import com.cloudBasedStorageService.StorageContainer.model.Folder;
import com.cloudBasedStorageService.StorageContainer.model.User;
import com.cloudBasedStorageService.StorageContainer.repo.FolderRepository;
import com.cloudBasedStorageService.StorageContainer.repo.UserRepository;
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

    public List<Folder> getFolder()
    {
        String userEmail = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
        User user=userRepository.findByUserEmail(userEmail).get();
        return folderRepository.findByCreatedBy(user);
    }

    public void saveFolder(Folder folder) {
        String userEmail = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
        User user=userRepository.findByUserEmail(userEmail).get();
        folder.setCreatedBy(user);
        folderRepository.save(folder);
    }

    public List<Folder> getFolderChildren(Integer parentFolderId) {
        Folder parentFolder=folderRepository.findById(parentFolderId).get();
        return folderRepository.findByParentFolder(parentFolder);
    }
}
