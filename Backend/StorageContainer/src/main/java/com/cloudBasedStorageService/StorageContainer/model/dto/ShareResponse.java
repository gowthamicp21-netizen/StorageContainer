package com.cloudBasedStorageService.StorageContainer.model.dto;

import com.cloudBasedStorageService.StorageContainer.model.ItemType;
import com.cloudBasedStorageService.StorageContainer.model.Role;

public record ShareResponse(
        Integer shareId,
        String email,
        Integer itemId,
        ItemType itemType,
        Role permission,
        String itemName,
        String shareBy
) {
}