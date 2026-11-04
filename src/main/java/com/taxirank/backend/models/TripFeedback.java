package com.taxirank.backend.models;

import com.taxirank.backend.enums.QueueReport;
import com.taxirank.backend.enums.RideRating;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** A rider's optional answers after a trip. At most one per trip. */
@Entity
@Table(name = "trip_feedback")
@Getter
@Setter
public class TripFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id", nullable = false, unique = true)
    private Trip trip;

    // Copied from the trip so fare reports can be counted per route cheaply
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal_id")
    private Terminal terminal;

    @Column(name = "fare_paid", precision = 8, scale = 2)
    private BigDecimal farePaid;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private QueueReport queue;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private RideRating rating;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
