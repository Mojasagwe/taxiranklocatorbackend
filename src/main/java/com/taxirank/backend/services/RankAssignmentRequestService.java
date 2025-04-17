package com.taxirank.backend.services;

import java.util.List;

import com.taxirank.backend.dto.RankAssignmentRequestDTO;
import com.taxirank.backend.dto.RankAssignmentResponseDTO;
import com.taxirank.backend.enums.AdminRequestStatus;
import com.taxirank.backend.models.RankAssignmentRequest;

public interface RankAssignmentRequestService {
    // Submit a new rank assignment request
    RankAssignmentRequest submitRequest(RankAssignmentRequestDTO requestDTO, Long adminId);
    
    // Get all requests with a specific status
    List<RankAssignmentRequest> getRequestsByStatus(AdminRequestStatus status);
    
    // Get all requests for a specific admin
    List<RankAssignmentRequest> getRequestsByAdmin(Long adminId);
    
    // Get all requests for a specific rank
    List<RankAssignmentRequest> getRequestsByRank(Long rankId);
    
    // Get all requests for a specific rank by code
    List<RankAssignmentRequest> getRequestsByRankCode(String rankCode);
    
    // Get a specific request by ID
    RankAssignmentRequest getRequestById(Long requestId);
    
    // Review and respond to a request
    RankAssignmentRequest reviewRequest(Long requestId, RankAssignmentResponseDTO responseDTO, Long reviewerId);
    
    // Cancel a request (by the requesting admin)
    RankAssignmentRequest cancelRequest(Long requestId, Long adminId);
    
    // Check if a rank has any pending requests
    boolean hasPendingRequestsForRank(Long rankId);
    
    // Check if an admin has any pending requests for a specific rank
    boolean hasAdminPendingRequestForRank(Long adminId, Long rankId);
    
    // Check if an admin has any pending requests for a specific rank by code
    boolean hasAdminPendingRequestForRankByCode(Long adminId, String rankCode);
} 