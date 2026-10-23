package com.cloudBasedStorageService.StorageContainer.controller;

import com.cloudBasedStorageService.StorageContainer.model.dto.PublicShareView;
import com.cloudBasedStorageService.StorageContainer.service.ShareLinkService;
import com.cloudBasedStorageService.StorageContainer.util.ContentResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The only unauthenticated surface in the API, so every handler here takes a link
 * token and proves the requested item sits inside that token's scope. Nothing accepts
 * an id without a token, which is what keeps a valid link from becoming a skeleton
 * key for the rest of the database.
 */
@RestController
@RequestMapping("/api/public/share")
public class PublicShareController {

    private final ShareLinkService shareLinkService;

    public PublicShareController(ShareLinkService shareLinkService) {
        this.shareLinkService = shareLinkService;
    }

    @GetMapping("/{token}")
    public PublicShareView view(@PathVariable String token) {

        return shareLinkService.view(token);
    }

    @GetMapping("/{token}/folder/{folderId}")
    public PublicShareView viewSubfolder(
            @PathVariable String token,
            @PathVariable Integer folderId) {

        return shareLinkService.viewSubfolder(token, folderId);
    }

    @GetMapping("/{token}/file/{fileId}/view")
    public ResponseEntity<Resource> viewFile(
            @PathVariable String token,
            @PathVariable Integer fileId) {

        return ContentResponse.of(
                shareLinkService.resolveFile(token, fileId), false);
    }

    @GetMapping("/{token}/file/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String token,
            @PathVariable Integer fileId) {

        return ContentResponse.of(
                shareLinkService.resolveFile(token, fileId), true);
    }
}
