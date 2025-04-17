package com.taxirank.backend.models;

import java.time.LocalDateTime;

import com.taxirank.backend.enums.AdminRequestStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a request from an admin to be assigned to a taxi rank
 */
@Entity
@Table(name = "rank_assignment_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RankAssignmentRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private RankAdmin requestingAdmin;
    
    @ManyToOne
    @JoinColumn(name = "rank_id", nullable = false)
    private TaxiRank requestedRank;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AdminRequestStatus status;
    
    @Column(length = 500)
    private String requestReason;
    
    @Column(length = 500)
    private String responseMessage;
    
    @ManyToOne
    @JoinColumn(name = "reviewer_id")
    private User reviewedBy;
    
    @Column(nullable = false)
    private LocalDateTime requestDate;
    
    private LocalDateTime responseDate;
} 