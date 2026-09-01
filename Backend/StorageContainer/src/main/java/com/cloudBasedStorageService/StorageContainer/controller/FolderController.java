package com.cloudBasedStorageService.StorageContainer.controller;

import com.cloudBasedStorageService.StorageContainer.model.Folder;
import com.cloudBasedStorageService.StorageContainer.service.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/folders")
public class FolderController {

    @Autowired
    private FolderService folderService;

    @GetMapping("/root")
    public ResponseEntity<?> getFolder(){
        List<Folder> folders=folderService.getFolder();
        return ResponseEntity.ok().body(folders);
    }
    @PostMapping
    public ResponseEntity<?> saveFolder(@RequestBody Folder folder){
        folderService.saveFolder(folder);
        return ResponseEntity.status(HttpStatus.CREATED).body("Successfully saved folder");
    }

    @GetMapping("/{parentFolderId}/children")
    public  ResponseEntity<?> getFolderChildren(@PathVariable Integer parentFolderId){
        System.out.println(parentFolderId);
        List<Folder> folders=folderService.getFolderChildren(parentFolderId);
        return ResponseEntity.ok().body(folders);
    }


}
