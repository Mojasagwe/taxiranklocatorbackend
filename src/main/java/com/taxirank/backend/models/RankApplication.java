package com.taxirank.backend.models;

import com.taxirank.backend.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * A person's application to manage a rank: either an existing rank (rank set) or
 * a rank that isn't in Travy yet (proposed* fields). Approving it creates the
 * rank if needed and the rank's shared Rank Admin account.
 */
@Entity
@Table(name = "rank_applications")
@Getter
@Setter
public class RankApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rank_id")
    private TaxiRank rank;

    @Column(name = "proposed_name", length = 100)
    private String proposedName;

    @Column(name = "proposed_address", length = 200)
    private String proposedAddress;

    @Column(name = "proposed_city", length = 100)
    private String proposedCity;

    @Column(name = "proposed_province", length = 100)
    private String proposedProvince;

    @Column(name = "proposed_latitude")
    private Double proposedLatitude;

    @Column(name = "proposed_longitude")
    private Double proposedLongitude;

    @Column(name = "proposed_operating_hours", length = 50)
    private String proposedOperatingHours;

    @Column(name = "proposed_capacity")
    private Integer proposedCapacity;

    @Column(name = "proposed_contact_phone", length = 30)
    private String proposedContactPhone;

    @Column(name = "proposed_contact_email")
    private String proposedContactEmail;

    @Column(nullable = false, length = 100)
    private String designation;

    @Column(length = 1000)
    private String motivation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status;

    // Latest message from the reviewer: the info request or the rejection reason
    @Column(name = "reviewer_note", length = 1000)
    private String reviewerNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rank_account_id")
    private User rankAccount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public boolean isProposal() {
        return rank == null;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (status == null) status = ApplicationStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
