package com.taxirank.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    // Email for people, rank code for rank accounts. "email" is accepted for older app builds.
    private String identifier;
    private String email;

    @NotBlank
    private String password;

    public String getLoginName() {
        return identifier != null && !identifier.isBlank() ? identifier.trim() : (email == null ? null : email.trim());
    }
}
