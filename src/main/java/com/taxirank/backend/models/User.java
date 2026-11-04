package com.taxirank.backend.models;

import com.taxirank.backend.enums.AccountStatus;
import com.taxirank.backend.enums.Permission;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * A login. Either a person (commuter or system administrator, identified by email)
 * or a rank account: the single shared login of one taxi rank, identified by the
 * rank code as username and owned by the rank rather than a person.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(unique = true)
    private String email;

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    // Login name for rank accounts (the rank code); people log in with their email
    @Column(unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "profile_picture")
    private String profilePicture;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status")
    private AccountStatus accountStatus;

    @Column(name = "status_reason", length = 500)
    private String statusReason;

    @Column(name = "must_change_password", nullable = false)
    private boolean mustChangePassword;

    // Set only on rank accounts; unique, so each rank has at most one account
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rank_id", unique = true)
    private TaxiRank rank;

    @Column(name = "responsible_person_name", length = 200)
    private String responsiblePersonName;

    @Column(name = "responsible_person_phone", length = 30)
    private String responsiblePersonPhone;

    // Bumped to revoke every token issued before (password change, reset, deletion)
    @Column(name = "token_version", nullable = false)
    private int tokenVersion;

    @Column(name = "notify_application_updates", nullable = false)
    private boolean notifyApplicationUpdates = true;

    @Column(name = "notify_rank_changes", nullable = false)
    private boolean notifyRankChanges = true;

    @Column(name = "notify_news", nullable = false)
    private boolean notifyNews;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "is_verified")
    private Boolean isVerified;

    @Column(name = "rating", columnDefinition = "numeric(2,1)")
    private Double rating;

    @Column(name = "total_trips")
    private Integer totalTrips;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean isRankAccount() {
        return rank != null;
    }

    public String getLoginName() {
        return username != null ? username : email;
    }

    public boolean isDeleted() {
        return accountStatus == AccountStatus.DELETED;
    }

    /** Invalidates every token issued so far */
    public void revokeTokens() {
        tokenVersion++;
    }

    public String getDisplayName() {
        return lastName == null || lastName.isBlank() ? firstName : firstName + " " + lastName;
    }

    public boolean hasProfile(String code) {
        return profile != null && code.equals(profile.getCode());
    }

    public Set<Permission> getPermissions() {
        return profile == null ? Set.of() : profile.getPermissions();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (rating == null) rating = 0.0;
        if (totalTrips == null) totalTrips = 0;
        if (isVerified == null) isVerified = false;
        if (accountStatus == null) accountStatus = AccountStatus.ACTIVE;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
