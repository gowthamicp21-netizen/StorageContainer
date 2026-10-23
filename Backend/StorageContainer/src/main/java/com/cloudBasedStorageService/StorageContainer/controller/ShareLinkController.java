package com.cloudBasedStorageService.StorageContainer.controller;

import com.cloudBasedStorageService.StorageContainer.model.ItemType;
import com.cloudBasedStorageService.StorageContainer.model.User;
import com.cloudBasedStorageService.StorageContainer.model.dto.ShareLinkRequest;
import com.cloudBasedStorageService.StorageContainer.model.dto.ShareLinkResponse;
import com.cloudBasedStorageService.StorageContainer.service.ShareLinkService;
import com.cloudBasedStorageService.StorageContainer.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/share-links")
public class ShareLinkController {

    private final ShareLinkService shareLinkService;
    private final UserService userService;

    public ShareLinkController(
            ShareLinkService shareLinkService,
            UserService userService) {

        this.shareLinkService = shareLinkService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ShareLinkResponse> create(@Valid @RequestBody ShareLinkRequest request) {

        return ResponseEntity.ok(shareLinkService.create(
                request.itemId(), request.itemType(), getLoggedInUser()));
    }

    /**
     * 404 when the item has no link, which the modal reads as "sharing is off".
     */
    @GetMapping
    public ResponseEntity<ShareLinkResponse> find(
            @RequestParam Integer itemId,
            @RequestParam ItemType itemType) {

        return ResponseEntity.ok(shareLinkService.find(
                itemId, itemType, getLoggedInUser()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revoke(@PathVariable Integer id) {

        shareLinkService.revoke(id, getLoggedInUser());

        return ResponseEntity.noContent().build();
    }

    private User getLoggedInUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        return userService.getUserByUsername(authentication.getName());
    }
}
