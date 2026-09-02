package com.cloudBasedStorageService.StorageContainer.controller;

import com.cloudBasedStorageService.StorageContainer.model.Folder;
import com.cloudBasedStorageService.StorageContainer.model.User;
import com.cloudBasedStorageService.StorageContainer.service.FolderService;
import com.cloudBasedStorageService.StorageContainer.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/folders")
public class FolderController {

    @Autowired
    private FolderService folderService;

    @Autowired
    private UserService userService;

    @GetMapping("/root")
    public ResponseEntity<?> getFolder(){
        List<Folder> folders=folderService.getFolder();
        return ResponseEntity.ok().body(folders);
    }

    @PutMapping("/folder/{folderId}/rename")
    public ResponseEntity<?> renameFolder(
            @PathVariable Integer folderId,
            @RequestParam String newName) {

        try {
            Folder folder = folderService.renameFolder(
                    folderId,
                    newName,
                    getLoggedInUser()
            );

            return ResponseEntity.ok(folder);

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
    @PostMapping
    public ResponseEntity<?> saveFolder(@RequestBody Folder folder){
        System.out.println(folder.getParentFolder());
        folderService.saveFolder(folder);
        return ResponseEntity.status(HttpStatus.CREATED).body("Successfully saved folder");
    }

    @GetMapping("/{parentFolderId}/children")
    public  ResponseEntity<?> getFolderChildren(@PathVariable Integer parentFolderId){
        List<Folder> folders=folderService.getFolderChildren(parentFolderId);
        return ResponseEntity.ok().body(folders);
    }

    @DeleteMapping("{folderId}")
    public ResponseEntity<?> deleteFolder(@PathVariable Integer folderId){

        try {

            folderService.deleteFolder(folderId);

            return ResponseEntity.ok(
                    "Folder deleted successfully"
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    private User getLoggedInUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        User user = userService.getUserByUsername(userEmail);
        return user;
    }


}
