package com.cloudBasedStorageService.StorageContainer.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email String userEmail,

        // BCrypt ignores bytes past 72, so reject longer input instead of
        // accepting a password that will not match on a later login.
        @NotBlank
        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
        String userPassword
) {
}
