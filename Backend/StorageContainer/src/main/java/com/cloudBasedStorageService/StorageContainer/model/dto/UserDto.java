package com.cloudBasedStorageService.StorageContainer.model.dto;

import com.cloudBasedStorageService.StorageContainer.model.Role;

/**
 * Safe projection of a user. The entity itself carries the password hash and
 * must never be serialized to a client.
 */
public record UserDto(Integer userId, String userEmail, Role userRole) {

    public static UserDto from(com.cloudBasedStorageService.StorageContainer.model.User user) {
        return new UserDto(user.getUserId(), user.getUserEmail(), user.getUserRole());
    }
}
