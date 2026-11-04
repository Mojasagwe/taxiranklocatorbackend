package com.taxirank.backend.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    // Where the route ends. Null point: matched through endsAtRank's location, if any.
    @Column(name = "destination_latitude")
    private Double destinationLatitude;

    @Column(name = "destination_longitude")
    private Double destinationLongitude;

    @Column(name = "destination_place_id")
    private String destinationPlaceId;

    // The rank this route ends at, which lets riders change there for a further destination
    @ManyToOne
    @JoinColumn(name = "ends_at_rank_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TaxiRank endsAtRank;

    @Column(length = 50)
    private String bay;

    @Column(name = "interval_minutes")
    private Integer intervalMinutes;

    @Column(name = "paused_on")
    private LocalDate pausedOn;

    @Column(name = "first_taxi")
    private LocalTime firstTaxi;

    @Column(name = "last_taxi_weekday")
    private LocalTime lastTaxiWeekday;

    @Column(name = "last_taxi_saturday")
    private LocalTime lastTaxiSaturday;

    @Column(name = "last_taxi_sunday")
    private LocalTime lastTaxiSunday;

    @Column(name = "fare_confirmed_at")
    private LocalDateTime fareConfirmedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Active, and not paused by the rank for that date */
    public boolean isRunningOn(LocalDate date) {
        return isActive && !date.equals(pausedOn);
    }

    /** The rank's stated last taxi for that day, if it set one */
    public LocalTime lastTaxiOn(DayOfWeek day) {
        return switch (day) {
            case SATURDAY -> lastTaxiSaturday;
            case SUNDAY -> lastTaxiSunday;
            default -> lastTaxiWeekday;
        };
    }

    /** Where the route ends on the map: its own point, else the rank it ends at; null if neither is known */
    public double[] destinationPoint() {
        if (destinationLatitude != null && destinationLongitude != null) {
            return new double[] {destinationLatitude, destinationLongitude};
        }
        if (endsAtRank != null && endsAtRank.getLatitude() != null && endsAtRank.getLongitude() != null) {
            return new double[] {endsAtRank.getLatitude(), endsAtRank.getLongitude()};
        }
        return null;
    }

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