package com.cloudBasedStorageService.StorageContainer.controller;

import com.cloudBasedStorageService.StorageContainer.model.Share;
import com.cloudBasedStorageService.StorageContainer.model.dto.ShareRequest;
import com.cloudBasedStorageService.StorageContainer.model.dto.ShareResponse;
import com.cloudBasedStorageService.StorageContainer.model.dto.SharedFolderResponse;
import com.cloudBasedStorageService.StorageContainer.service.ShareService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shares")
public class ShareController {

    private final ShareService shareService;

    public ShareController(ShareService shareService) {
        this.shareService = shareService;
    }

    @PostMapping
    public ResponseEntity<Share> createShare(@Valid @RequestBody ShareRequest request) {

        return ResponseEntity.ok(shareService.createShare(request));
    }

    @GetMapping("/shared-with-me")
    public ResponseEntity<List<ShareResponse>> findSharedWithMe() {

        return ResponseEntity.ok(shareService.findSharedWithMe());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShare(@PathVariable Integer id) {

        shareService.deleteShare(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/folder/{folderId}")
    public ResponseEntity<SharedFolderResponse> getSharedFolder(
            @PathVariable Integer folderId) {

        return ResponseEntity.ok(shareService.getSharedFolder(folderId));
    }
}
