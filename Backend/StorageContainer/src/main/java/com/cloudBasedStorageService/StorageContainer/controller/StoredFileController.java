package com.cloudBasedStorageService.StorageContainer.controller;

import com.cloudBasedStorageService.StorageContainer.model.File;
import com.cloudBasedStorageService.StorageContainer.model.User;
import com.cloudBasedStorageService.StorageContainer.service.StoredFileService;
import com.cloudBasedStorageService.StorageContainer.service.UserService;
import com.cloudBasedStorageService.StorageContainer.util.ContentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class StoredFileController {

    private final StoredFileService storedFileService;
    private final UserService userService;

    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public File uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folderId", required = false) Integer folderId) {

        return storedFileService.uploadFile(file, folderId, getLoggedInUser());
    }

    @PutMapping("/file/{fileId}/rename")
    public File renameFile(
            @PathVariable Integer fileId,
            @RequestParam String newName) {

        return storedFileService.renameFile(fileId, newName, getLoggedInUser());
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable int fileId) {

        return ContentResponse.of(
                storedFileService.downloadFile(fileId, getLoggedInUser()), true);
    }

    @GetMapping("/view/{fileId}")
    public ResponseEntity<Resource> viewFile(@PathVariable int fileId) {

        return ContentResponse.of(
                storedFileService.viewFile(fileId, getLoggedInUser()), false);
    }

    @DeleteMapping("/file/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable int fileId) {

        storedFileService.deleteFile(fileId, getLoggedInUser());

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<File> getFiles(
            @RequestParam(value = "folderId", required = false) Integer folderId) {

        return storedFileService.getFiles(folderId, getLoggedInUser());
    }

    private User getLoggedInUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        return userService.getUserByUsername(authentication.getName());
    }
}
