package com.taxirank.backend.services;

import java.util.List;

import com.taxirank.backend.dto.AdminRegistrationRequest;
import com.taxirank.backend.dto.AdminReviewRequest;
import com.taxirank.backend.enums.AdminRequestStatus;

public interface AdminRegistrationService {
    // Submit a new admin registration request
    com.taxirank.backend.models.AdminRegistrationRequest submitRequest(AdminRegistrationRequest requestDTO);
    
    // Get all pending admin registration requests
    List<com.taxirank.backend.models.AdminRegistrationRequest> getPendingRequests();
    
    // Get all admin registration requests with a specific status
    List<com.taxirank.backend.models.AdminRegistrationRequest> getRequestsByStatus(AdminRequestStatus status);
    
    // Get a specific admin registration request by ID
    com.taxirank.backend.models.AdminRegistrationRequest getRequestById(Long id);
    
    // Review and update status of an admin registration request
    com.taxirank.backend.models.AdminRegistrationRequest reviewRequest(Long requestId, AdminReviewRequest reviewDTO, Long reviewerId);
    
    // Check if email is already used in a pending request
    boolean isPendingRequestExistsForEmail(String email);
    
    /**
     * Check if a rank already has an admin assigned to it using rank ID
     */
    boolean isRankAlreadyAssigned(Long rankId);
    
    /**
     * Check if a rank already has an admin assigned to it using rank code
     */
    boolean isRankAlreadyAssignedByCode(String rankCode);
}