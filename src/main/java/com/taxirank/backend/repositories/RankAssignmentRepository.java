package com.taxirank.backend.repositories;

import com.taxirank.backend.enums.AdminRequestStatus;
import com.taxirank.backend.models.RankAssignmentRequest;
import com.taxirank.backend.models.TaxiRank;
import com.taxirank.backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RankAssignmentRepository extends JpaRepository<RankAssignmentRequest, Long> {
    
    // Find all requests by status
    List<RankAssignmentRequest> findByStatus(AdminRequestStatus status);
    
    // Find all requests by a specific admin
    List<RankAssignmentRequest> findByRequestingAdmin(User requestingAdmin);
    
    // Find all requests for a specific rank
    List<RankAssignmentRequest> findByRequestedRank(TaxiRank requestedRank);
    
    // Find a specific request by admin and rank
    Optional<RankAssignmentRequest> findByRequestingAdminAndRequestedRank(User requestingAdmin, TaxiRank requestedRank);
    
    // Find pending requests by admin
    List<RankAssignmentRequest> findByRequestingAdminAndStatus(User requestingAdmin, AdminRequestStatus status);
    
    // Find pending requests for a specific rank
    List<RankAssignmentRequest> findByRequestedRankAndStatus(TaxiRank requestedRank, AdminRequestStatus status);
} 