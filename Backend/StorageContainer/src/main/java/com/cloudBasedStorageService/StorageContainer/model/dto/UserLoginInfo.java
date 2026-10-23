package com.cloudBasedStorageService.StorageContainer.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserLoginInfo(
        @NotBlank @Email String userEmail,
        @NotBlank String userPassword
) {
}
