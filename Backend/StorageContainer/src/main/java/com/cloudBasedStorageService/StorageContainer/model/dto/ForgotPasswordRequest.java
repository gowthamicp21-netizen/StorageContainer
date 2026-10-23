package com.cloudBasedStorageService.StorageContainer.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
        @NotBlank @Email String userEmail
) {
}
