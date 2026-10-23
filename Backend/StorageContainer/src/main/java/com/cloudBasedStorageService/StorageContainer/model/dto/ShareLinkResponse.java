package com.cloudBasedStorageService.StorageContainer.model.dto;

import com.cloudBasedStorageService.StorageContainer.model.ItemType;

import java.time.LocalDateTime;

/**
 * Carries the raw token rather than a finished URL: the frontend builds the share
 * address from its own origin, so a link copied from a LAN or deployed host still
 * points somewhere the visitor can actually reach.
 */
public record ShareLinkResponse(
        Integer id,
        String token,
        Integer itemId,
        ItemType itemType,
        String itemName,
        String ownerEmail,
        LocalDateTime createdAt
) {
}
