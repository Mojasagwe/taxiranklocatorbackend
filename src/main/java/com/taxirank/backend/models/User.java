package com.taxirank.backend.models;

import com.taxirank.backend.enums.AccountStatus;
import com.taxirank.backend.enums.PaymentMethod;
import com.taxirank.backend.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;

    @Column(nullable = false)
    private String password;

    @Column(name = "profile_picture")
    private String profilePicture;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_payment_method")
    private PaymentMethod preferredPaymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status")
    private AccountStatus accountStatus;

    @Column(name = "verification_status")
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
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private UserRole role = UserRole.RIDER;
    
    // Relationship with RankAdmin
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("user")
    private Set<RankAdmin> managedRanks = new HashSet<>();

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