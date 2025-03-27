package com.taxirank.backend.models;

import com.taxirank.backend.enums.AdminRequestStatus;
import com.taxirank.backend.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a pending admin registration request.
 */
@Entity
@Table(name = "admin_registration_requests")
@Data
public class AdminRegistrationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // User information
    @Column(nullable = false)
    private String firstName;
    
    @Column(nullable = false)
    private String lastName;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_payment_method")
    private PaymentMethod preferredPaymentMethod;
    
    // Admin specific information
    private String designation;
    
    @Column(length = 1000)
    private String justification;
    
    @Column(length = 1000)
    private String professionalExperience;
    
    @Column(length = 500)
    private String adminNotes;
    
    // Request status
    @Enumerated(EnumType.STRING)
    private AdminRequestStatus status = AdminRequestStatus.PENDING;
    
    // Review information
    @Column(length = 500)
    private String reviewNotes;
    
    @ManyToOne
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;
    
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    
    // Relationship with taxi ranks
    @ManyToMany
    @JoinTable(
        name = "admin_request_ranks",
        joinColumns = @JoinColumn(name = "request_id"),
        inverseJoinColumns = @JoinColumn(name = "rank_id")
    )
    private Set<TaxiRank> selectedRanks = new HashSet<>();
    
    // Audit fields
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}