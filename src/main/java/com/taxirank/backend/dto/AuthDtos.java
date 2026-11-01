package com.taxirank.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request/response bodies for /api/auth */
public final class AuthDtos {
    private AuthDtos() {
    }

    public record AuthResponse(UserResponse user, String token) {
    }

    public record ChangePasswordRequest(
            @NotBlank String currentPassword,
            @NotBlank @Size(min = 8, max = 128, message = "must be 8 to 128 characters") String newPassword) {
    }
}
