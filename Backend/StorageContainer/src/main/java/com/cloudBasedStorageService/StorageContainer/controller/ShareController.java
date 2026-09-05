package com.cloudBasedStorageService.StorageContainer.controller;

import com.cloudBasedStorageService.StorageContainer.model.Folder;
import com.cloudBasedStorageService.StorageContainer.model.dto.ShareResponse;
import com.cloudBasedStorageService.StorageContainer.model.dto.SharedFolderResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.cloudBasedStorageService.StorageContainer.model.Share;
import com.cloudBasedStorageService.StorageContainer.service.ShareService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/shares")
public class ShareController {

    @Autowired
    private  ShareService shareService;

    @PostMapping
    public ResponseEntity<Share> createShare(@RequestBody Share share) {

        Share savedShare = shareService.createShare(share);

        return ResponseEntity.ok(savedShare);
    }

    @GetMapping("/shared-with-me")
    public ResponseEntity<List<ShareResponse>> findSharedWithMe(){
        List<ShareResponse> shares=shareService.findSharedWithMe();
        return ResponseEntity.ok(shares);
    }


    @GetMapping
    public ResponseEntity<List<Share>> getAllShares() {

        return ResponseEntity.ok(
                shareService.getAllShares()
        );
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShare(@PathVariable Integer id) {

        shareService.deleteShare(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/folder/{folderId}")
    public ResponseEntity<SharedFolderResponse> getSharedFolder(
            @PathVariable Integer folderId) {

        return ResponseEntity.ok(
                shareService.getSharedFolder(folderId)
        );
    }
}
