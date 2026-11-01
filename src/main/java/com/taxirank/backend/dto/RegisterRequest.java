package com.taxirank.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    @Size(max = 100)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    private String lastName;

    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    @NotBlank
    @Size(min = 8, max = 128, message = "must be 8 to 128 characters")
    private String password;

    @NotBlank
    @Pattern(regexp = "^\\+?[0-9 ]{9,20}$", message = "must be a valid phone number")
    private String phoneNumber;
}
