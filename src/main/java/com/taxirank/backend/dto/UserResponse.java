package com.taxirank.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.taxirank.backend.enums.AccountStatus;
import com.taxirank.backend.models.TaxiRank;
import com.taxirank.backend.models.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * The only shape in which accounts leave the API. Never carries the password hash.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String displayName,
        String email,
        String phoneNumber,
        String loginName,
        String profileCode,
        String profileName,
        List<String> permissions,
        AccountStatus accountStatus,
        String statusReason,
        boolean mustChangePassword,
        RankSummary rank,
        String responsiblePersonName,
        String responsiblePersonPhone,
        // Relative to the API base, e.g. /api/users/7/photo?v=1b2c...; changes with every new photo
        String photoUrl,
        LocalDateTime lastLogin,
        LocalDateTime createdAt) {

    public record RankSummary(Long id, String code, String name, String city) {
        public static RankSummary from(TaxiRank rank) {
            return rank == null ? null : new RankSummary(rank.getId(), rank.getCode(), rank.getName(), rank.getCity());
        }
    }

    private static String photoUrl(User user) {
        String key = user.getProfilePicture();
        if (key == null) {
            return null;
        }
        return "/api/users/" + user.getId() + "/photo?v=" + key.substring(0, Math.min(8, key.length()));
    }

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getDisplayName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getLoginName(),
                user.getProfile().getCode(),
                user.getProfile().getName(),
                user.getPermissions().stream().map(Enum::name).sorted().toList(),
                user.getAccountStatus(),
                user.getStatusReason(),
                user.isMustChangePassword(),
                RankSummary.from(user.getRank()),
                user.getResponsiblePersonName(),
                user.getResponsiblePersonPhone(),
                photoUrl(user),
                user.getLastLogin(),
                user.getCreatedAt());
    }
}
