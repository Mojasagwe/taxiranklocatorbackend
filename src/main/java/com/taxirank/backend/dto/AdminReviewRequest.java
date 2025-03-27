package com.taxirank.backend.dto;

import com.taxirank.backend.enums.AdminRequestStatus;
import lombok.Data;

/**
 * DTO for reviewing an admin registration request.
 */
@Data
public class AdminReviewRequest {
    // The new status for the request (APPROVED or REJECTED)
    private AdminRequestStatus status;
    
    // Notes about the decision
    private String reviewNotes;
}