package com.taxirank.backend.models;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Duration;

@Entity
@Table(name = "routes")
@Data
public class Route {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "source_rank_id", nullable = false)
    private Rank sourceRank;
    
    @ManyToOne
    @JoinColumn(name = "destination_rank_id", nullable = false)
    private Rank destinationRank;
    
    @Column(nullable = false)
    private BigDecimal fare;
    
    private Duration estimatedDuration;
    
    private Double distance;
    
    @Column(name = "traffic_status")
    private String trafficStatus;
    
    @Column(name = "peak_hours")
    private String peakHours;
    
    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        updatedAt = createdAt;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }
} 