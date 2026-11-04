package com.taxirank.backend.models;

import com.taxirank.backend.enums.TripStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A trip a rider planned or took from a rank to one of its destinations. The destination
 * name and fare are copied at logging time, so history stays right when the rank changes them.
 * user is null once the rider deletes their account (kept only for anonymous counts).
 */
@Entity
@Table(name = "trips")
@Getter
@Setter
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "rank_id", nullable = false)
    private TaxiRank rank;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal_id")
    private Terminal terminal;

    @Column(nullable = false)
    private String destination;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal fare;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TripStatus status;

    @Column(name = "trip_date", nullable = false)
    private LocalDateTime tripDate;

    // Riders' taps; wait time = departedAt - boardedAt
    @Column(name = "boarded_at")
    private LocalDateTime boardedAt;

    @Column(name = "departed_at")
    private LocalDateTime departedAt;

    @Column(name = "arrived_at")
    private LocalDateTime arrivedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
