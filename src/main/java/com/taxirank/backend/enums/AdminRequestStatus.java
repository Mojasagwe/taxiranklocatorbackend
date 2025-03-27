package com.taxirank.backend.enums;

/**
 * Enum representing the different statuses for admin registration requests.
 */
public enum AdminRequestStatus {
    /**
     * Request is awaiting review by a super admin
     */
    PENDING,
    
    /**
     * Request has been approved
     */
    APPROVED,
    
    /**
     * Request has been rejected
     */
    REJECTED
}