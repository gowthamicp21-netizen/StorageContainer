package com.cloudBasedStorageService.StorageContainer.service;

import com.cloudBasedStorageService.StorageContainer.model.*;
import com.cloudBasedStorageService.StorageContainer.model.dto.*;
import com.cloudBasedStorageService.StorageContainer.model.dto.SharedFolderResponse;
import com.cloudBasedStorageService.StorageContainer.repo.FileRepository;
import com.cloudBasedStorageService.StorageContainer.repo.FolderRepository;
import com.cloudBasedStorageService.StorageContainer.repo.ShareRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ShareService {

    @Autowired
    private ShareRepository shareRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FolderRepository folderRepository;

    public Share createShare(Share share) {

        User loggedInUser = getLoggedInUser();


        if (loggedInUser.getUserEmail().equalsIgnoreCase(share.getEmail())) {
            throw new RuntimeException("You cannot share with yourself");
        }


        if (share.getItemType() == ItemType.FILE) {

            File file = fileRepository.findById(share.getItemId())
                    .orElseThrow(() ->
                            new RuntimeException("File not found"));

            if (!file.getCreatedBy().getUserId()
                    .equals(loggedInUser.getUserId())) {

                throw new RuntimeException(
                        "You are not allowed to share this file");
            }

        } else if (share.getItemType() == ItemType.FOLDER) {

            Folder folder = folderRepository.findById(share.getItemId())
                    .orElseThrow(() ->
                            new RuntimeException("Folder not found"));

            if (!folder.getCreatedBy().getUserId()
                    .equals(loggedInUser.getUserId())) {

                throw new RuntimeException(
                        "You are not allowed to share this folder");
            }
        }

        share.setSharedBy(loggedInUser);

        return shareRepository.save(share);
    }

    public List<ShareResponse> findSharedWithMe() {

        User user = getLoggedInUser();

        List<Share> shares =
                shareRepository.findAllByEmail(user.getUserEmail());

        List<ShareResponse> response = new ArrayList<>();

        for (Share share : shares) {


            if (share.getItemType().equals(ItemType.FILE)) {

                fileRepository.findById(share.getItemId())
                        .ifPresent(file -> {

                            ShareResponse res = new ShareResponse(
                                    share.getId(),
                                    share.getEmail(),
                                    share.getItemId(),
                                    share.getItemType(),
                                    share.getPermission(),
                                    file.getFileName(),
                                    share.getSharedBy().getUserEmail()
                            );

                            response.add(res);
                        });

            }

            // ==============================
            // FOLDER
            // ==============================

            else {

                folderRepository.findById(share.getItemId())
                        .ifPresent(folder -> {

                            ShareResponse res = new ShareResponse(
                                    share.getId(),
                                    share.getEmail(),
                                    share.getItemId(),
                                    share.getItemType(),
                                    share.getPermission(),
                                    folder.getFolderName(),
                                    share.getSharedBy().getUserEmail()
                            );

                            response.add(res);
                        });
            }
        }

        return response;
    }
    public List<Share> getAllShares() {
        return shareRepository.findAll();
    }

    public void deleteShare(Integer id) {
        shareRepository.deleteById(id);
    }

    private User getLoggedInUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        User user = userService.getUserByUsername(userEmail);
        return user;
    }

    public SharedFolderResponse getSharedFolder(Integer folderId) {

        User user = getLoggedInUser();

        if (!hasFolderAccess(
                folderId,
                user.getUserEmail()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not have access to this folder"
            );
        }

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Folder not found"
                        )
                );

        List<Folder> folders =
                folderRepository.findByParentFolder(folder);

        List<File> files =
                fileRepository.findByFolder(folder);

        return new SharedFolderResponse(
                folder,
                folders,
                files
        );
    }

    public boolean hasFolderAccess(Integer folderId, String userEmail) {

        Folder folder = folderRepository.findById(folderId)
                .orElse(null);

        while (folder != null) {

            boolean shared = shareRepository
                    .findByEmailAndItemIdAndItemType(
                            userEmail,
                            folder.getId(),
                            ItemType.FOLDER
                    )
                    .isPresent();

            if (shared) {
                return true;
            }

            folder = folder.getParentFolder();
        }

        return false;
    }


}
