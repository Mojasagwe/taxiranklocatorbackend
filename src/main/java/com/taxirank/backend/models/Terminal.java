package com.taxirank.backend.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "terminals")
@Data
public class Terminal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double fare;

    @Column(name = "travel_time", nullable = false)
    private String travelTime;

    @Column(nullable = false)
    private String distance;

    @Column(name = "departure_schedule")
    private String departureSchedule;

    @Column(name = "operating_days")
    private String operatingDaysString;
    
    @Transient
    private List<String> operatingDays;
    
    public List<String> getOperatingDays() {
        if (operatingDaysString != null && !operatingDaysString.isEmpty()) {
            return Arrays.asList(operatingDaysString.split(","));
        }
        return null;
    }
    
    public void setOperatingDays(List<String> operatingDays) {
        this.operatingDays = operatingDays;
        if (operatingDays != null && !operatingDays.isEmpty()) {
            this.operatingDaysString = String.join(",", operatingDays);
        } else {
            this.operatingDaysString = null;
        }
    }

    @Column(name = "is_active")
    private boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "taxi_rank_id", nullable = false)
    private TaxiRank taxiRank;

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