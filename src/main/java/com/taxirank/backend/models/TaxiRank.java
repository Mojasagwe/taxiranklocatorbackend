package com.taxirank.backend.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a taxi rank location.
 */
@Entity
@Table(name = "taxi_ranks")
@Data
public class TaxiRank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String code;
    
    private String description;
    
    @Column(nullable = false)
    private String address;
    
    private String city;
    
    private String province;
    
    // Geographic coordinates
    private Double latitude;
    private Double longitude;
    
    // Contact information
    private String contactPhone;
    private String contactEmail;
    
    // Operational details
    private String operatingHours;
    private Integer capacity;
    private Boolean isActive = true;
    
    // Audit fields
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "taxiRank")
    private Set<RankAdmin> rankAdmins = new HashSet<>();
    
    @OneToMany(mappedBy = "taxiRank", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Terminal> terminals = new HashSet<>();
    
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