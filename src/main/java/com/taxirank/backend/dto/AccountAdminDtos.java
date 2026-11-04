package com.taxirank.backend.dto;

import com.taxirank.backend.enums.AccountStatus;
import com.taxirank.backend.models.AccountEvent;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

/** Request/response bodies for account management by System Administrators: /api/admin/users */
public final class AccountAdminDtos {
    private AccountAdminDtos() {
    }

    static final String PHONE = "^\\+?[0-9 ]{9,20}$";

    /**
     * Rank accounts (profileCode RANK_ADMIN) need rankId and a responsible person;
     * the rank code becomes the login and names/email/phone come from the rank.
     * Other profiles need first name, last name, email and phone.
     */
    public record CreateUserRequest(
            @Size(max = 100) String firstName,
            @Size(max = 100) String lastName,
            @Email @Size(max = 255) String email,
            @Pattern(regexp = PHONE, message = "must be a valid phone number") String phoneNumber,
            @NotBlank String profileCode,
            Long rankId,
            @Size(max = 200) String responsiblePersonName,
            @Pattern(regexp = PHONE, message = "must be a valid phone number") String responsiblePersonPhone) {
    }

    /** Null fields are left unchanged */
    public record UpdateUserRequest(
            @Size(min = 1, max = 100) String firstName,
            @Size(max = 100) String lastName,
            @Email @Size(max = 255) String email,
            @Pattern(regexp = PHONE, message = "must be a valid phone number") String phoneNumber,
            String profileCode) {
    }

    public record StatusRequest(@NotNull AccountStatus status, @Size(max = 500) String reason) {
    }

    public record ResponsiblePersonRequest(
            @NotBlank @Size(max = 200) String name,
            @NotBlank @Pattern(regexp = PHONE, message = "must be a valid phone number") String phone) {
    }

    /** Returned once when an account is created or its password reset; the password is never retrievable again */
    public record IssuedCredentials(UserResponse user, String loginName, String temporaryPassword) {
    }

    public record EventResponse(String action, String details, String actorName, LocalDateTime createdAt) {
        public static EventResponse from(AccountEvent event) {
            return new EventResponse(event.getAction(), event.getDetails(),
                    event.getActor() == null ? "System" : event.getActor().getDisplayName(), event.getCreatedAt());
        }
    }

    public record UserDetailResponse(UserResponse user, List<EventResponse> history) {
    }
}
