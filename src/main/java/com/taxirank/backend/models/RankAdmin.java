package com.taxirank.backend.models;

import jakarta.persistence.*;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

/**
 * Junction table that associates users with admin roles to specific taxi ranks.
 * This allows tracking which admin manages which taxi rank and their specific permissions.
 */
@Entity
@Table(name = "rank_admins")
@Data
public class RankAdmin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties("managedRanks")
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "rank_id", nullable = false)
    private TaxiRank taxiRank;
    
    // Specific permissions for this admin at this rank
    private boolean canManageDrivers = true;
    private boolean canViewFinancials = true;
    private boolean canEditRankDetails = true;
    private boolean canManageRoutes = true;
    private boolean canManageTerminals = true;
    
    // Audit fields
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;
    
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
    
    // Designation or title at this rank
    private String designation;
    
    // Notes about this admin assignment
    @Column(length = 500)
    private String notes;
    
    @PrePersist
    protected void onCreate() {
        assignedAt = LocalDateTime.now();
        lastUpdated = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
} 