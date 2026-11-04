package com.taxirank.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.taxirank.backend.enums.ApplicationStatus;
import com.taxirank.backend.models.RankApplication;
import com.taxirank.backend.models.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

/** Request/response bodies for /api/applications */
public final class ApplicationDtos {
    private ApplicationDtos() {
    }

    /** A rank that isn't in Travy yet, as described by the applicant (or corrected by the reviewer) */
    public record ProposedRank(
            @NotBlank @Size(min = 3, max = 100) String name,
            @NotBlank @Size(max = 200) String address,
            @NotBlank @Size(max = 100) String city,
            @NotBlank @Size(max = 100) String province,
            @DecimalMin("-90") @DecimalMax("90") Double latitude,
            @DecimalMin("-180") @DecimalMax("180") Double longitude,
            @Size(max = 50) String operatingHours,
            @Positive Integer capacity,
            @Pattern(regexp = "^\\+?[0-9 ]{9,20}$", message = "must be a valid phone number") String contactPhone,
            @Email @Size(max = 255) String contactEmail) {
    }

    /** Exactly one of rankId or proposedRank */
    public record ApplicationRequest(
            Long rankId,
            @Valid ProposedRank proposedRank,
            @NotBlank @Size(max = 100) String designation,
            @Size(max = 1000) String motivation) {
    }

    public record NoteRequest(@NotBlank @Size(max = 1000) String note) {
    }

    public record LinkRequest(@NotNull Long rankId) {
    }

    /** For proposals: optional corrections to the rank and the code to give it */
    public record ApproveRequest(
            @Pattern(regexp = "^[A-Z0-9]{3,20}$", message = "must be 3-20 capital letters or digits") String code,
            @Valid ProposedRank rank) {
    }

    public record Applicant(Long id, String name, String email, String phoneNumber) {
        static Applicant from(User user) {
            return new Applicant(user.getId(), user.getDisplayName(), user.getEmail(), user.getPhoneNumber());
        }
    }

    public record NearbyRank(Long id, String code, String name, long distanceMeters, boolean hasAccount) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ApplicationResponse(
            Long id,
            ApplicationStatus status,
            Applicant applicant,
            UserResponse.RankSummary rank,
            ProposedRank proposedRank,
            String designation,
            String motivation,
            String reviewerNote,
            LocalDateTime reviewedAt,
            String rankAccountLoginName,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            List<NearbyRank> nearbyRanks) {

        public static ApplicationResponse from(RankApplication app, List<NearbyRank> nearbyRanks) {
            ProposedRank proposed = app.getProposedName() == null ? null : new ProposedRank(
                    app.getProposedName(), app.getProposedAddress(), app.getProposedCity(), app.getProposedProvince(),
                    app.getProposedLatitude(), app.getProposedLongitude(), app.getProposedOperatingHours(),
                    app.getProposedCapacity(), app.getProposedContactPhone(), app.getProposedContactEmail());
            return new ApplicationResponse(
                    app.getId(),
                    app.getStatus(),
                    Applicant.from(app.getApplicant()),
                    UserResponse.RankSummary.from(app.getRank()),
                    proposed,
                    app.getDesignation(),
                    app.getMotivation(),
                    app.getReviewerNote(),
                    app.getReviewedAt(),
                    app.getRankAccount() == null ? null : app.getRankAccount().getLoginName(),
                    app.getCreatedAt(),
                    app.getUpdatedAt(),
                    nearbyRanks);
        }
    }

    /** The rank account's login is shown exactly once, here */
    public record ApprovalResponse(ApplicationResponse application, String loginName, String temporaryPassword) {
    }
}
