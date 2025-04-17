package com.taxirank.backend.enums;

/**
 * Enum representing the possible statuses of a rank assignment request
 */
public enum AdminRequestStatus {
    PENDING,     // Request has been submitted but not yet reviewed
    APPROVED,    // Request has been reviewed and approved
    REJECTED,    // Request has been reviewed and rejected
    CANCELLED    // Request was cancelled by the requesting admin
}