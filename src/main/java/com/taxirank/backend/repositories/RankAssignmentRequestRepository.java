package com.taxirank.backend.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.taxirank.backend.enums.AdminRequestStatus;
import com.taxirank.backend.models.RankAdmin;
import com.taxirank.backend.models.RankAssignmentRequest;
import com.taxirank.backend.models.TaxiRank;

@Repository
public interface RankAssignmentRequestRepository extends JpaRepository<RankAssignmentRequest, Long> {
    
    // Find requests by their status
    List<RankAssignmentRequest> findByStatus(AdminRequestStatus status);
    
    // Find requests for a specific admin
    List<RankAssignmentRequest> findByRequestingAdmin(RankAdmin admin);
    
    // Find requests for a specific rank
    List<RankAssignmentRequest> findByRequestedRank(TaxiRank rank);
    
    // Find requests by admin and status
    List<RankAssignmentRequest> findByRequestingAdminAndStatus(RankAdmin admin, AdminRequestStatus status);
    
    // Find requests by rank and status
    List<RankAssignmentRequest> findByRequestedRankAndStatus(TaxiRank rank, AdminRequestStatus status);
    
    // Check if there's any pending request for a specific rank and admin
    boolean existsByRequestingAdminAndRequestedRankAndStatus(RankAdmin admin, TaxiRank rank, AdminRequestStatus status);
    
    // Delete all requests for a specific admin
    @Transactional
    void deleteByRequestingAdmin(RankAdmin admin);
} 