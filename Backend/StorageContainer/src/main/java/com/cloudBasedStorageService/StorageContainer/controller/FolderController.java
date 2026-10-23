package com.cloudBasedStorageService.StorageContainer.controller;

import com.cloudBasedStorageService.StorageContainer.model.Folder;
import com.cloudBasedStorageService.StorageContainer.model.User;
import com.cloudBasedStorageService.StorageContainer.model.dto.CreateFolderRequest;
import com.cloudBasedStorageService.StorageContainer.service.FileStorageService;
import com.cloudBasedStorageService.StorageContainer.service.FolderService;
import com.cloudBasedStorageService.StorageContainer.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/folders")
public class FolderController {

    private final FolderService folderService;
    private final UserService userService;
    private final FileStorageService fileStorageService;

    public FolderController(
            FolderService folderService,
            UserService userService,
            FileStorageService fileStorageService) {

        this.folderService = folderService;
        this.userService = userService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/root")
    public List<Folder> getRootFolders() {
        return folderService.getRootFolders();
    }

    @PutMapping("/folder/{folderId}/rename")
    public Folder renameFolder(
            @PathVariable Integer folderId,
            @RequestParam String newName) {

        return folderService.renameFolder(folderId, newName, getLoggedInUser());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Folder saveFolder(@Valid @RequestBody CreateFolderRequest request) {

        return folderService.saveFolder(request);
    }

    @GetMapping("/{folderId}/children")
    public List<Folder> getChildFolders(@PathVariable Integer folderId) {

        return folderService.getChildFolders(folderId);
    }

    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> deleteFolder(@PathVariable Integer folderId) {

        // Unlink the blobs only after the row transaction committed, otherwise a
        // rolled back delete would leave files pointing at missing content.
        folderService.deleteFolder(folderId, getLoggedInUser())
                .forEach(fileStorageService::delete);

        return ResponseEntity.noContent().build();
    }

    private User getLoggedInUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        return userService.getUserByUsername(authentication.getName());
    }
}
