package com.taxirank.backend.dto;

import com.taxirank.backend.models.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Request/response bodies for the signed-in user's own account: /api/users/me */
public final class MyAccountDtos {
    private MyAccountDtos() {
    }

    /**
     * Null fields are left unchanged. currentPassword is required when the email or
     * phone number changes, so an unlocked phone in the wrong hands can't take over the account.
     */
    public record UpdateMyDetailsRequest(
            @Size(min = 1, max = 100) String firstName,
            @Size(min = 1, max = 100) String lastName,
            @Email @Size(max = 255) String email,
            @Pattern(regexp = AccountAdminDtos.PHONE, message = "must be a valid phone number") String phoneNumber,
            @Size(max = 128) String currentPassword) {
    }

    public record NotificationSettings(
            @NotNull Boolean applicationUpdates,
            @NotNull Boolean rankChanges,
            @NotNull Boolean news) {

        public static NotificationSettings from(User user) {
            return new NotificationSettings(user.isNotifyApplicationUpdates(), user.isNotifyRankChanges(), user.isNotifyNews());
        }
    }

    /** confirmation must be the word DELETE */
    public record DeleteAccountRequest(
            @NotBlank @Size(max = 128) String password,
            @NotBlank @Size(max = 20) String confirmation) {
    }
}
