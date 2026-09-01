package com.cloudBasedStorageService.StorageContainer.controller;

import com.cloudBasedStorageService.StorageContainer.model.File;
import com.cloudBasedStorageService.StorageContainer.model.User;
import com.cloudBasedStorageService.StorageContainer.model.dto.FileDownloadResponse;
import com.cloudBasedStorageService.StorageContainer.service.StoredFileService;
import com.cloudBasedStorageService.StorageContainer.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class StoredFileController {

    private final StoredFileService storedFileService;

    private final UserService userService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam(value = "folderId", required = false) Integer folderId) {
        try {
            User user = getLoggedInUser();
            File uploadedFile = storedFileService.uploadFile(file, folderId, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(uploadedFile);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).
                    body("File upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<?> downloadFile(@PathVariable int fileId){
        try{
            User user = getLoggedInUser();
            FileDownloadResponse downloadedFile=storedFileService.downloadFile(fileId,user);

            System.out.println(downloadedFile.fileName());

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" +
                                    downloadedFile.fileName() + "\""
                    )
                    .contentType(
                            MediaType.parseMediaType(
                                    downloadedFile.fileType()
                            )
                    )
                    .body(downloadedFile.fileData());

        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).
                    body("File Download failed: " + e.getMessage());
        }
    }


    @GetMapping
    public ResponseEntity<List<File>> getFiles(
            @RequestParam(value = "folderId",
                    required = false)
            Integer folderId
    ) {

        User user = getLoggedInUser();

        return ResponseEntity.ok(
                storedFileService.getFiles(
                        folderId,
                        user
                )
        );
    }

    private User getLoggedInUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        User user = userService.getUserByUsername(userEmail);
        return user;
    }
}
