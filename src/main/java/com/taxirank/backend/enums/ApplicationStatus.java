package com.taxirank.backend.enums;

import java.util.EnumSet;
import java.util.Set;

/**
 * Lifecycle of an application to manage a rank
 */
public enum ApplicationStatus {
    PENDING,         // Waiting for a System Administrator
    INFO_REQUESTED,  // Sent back to the applicant with a note
    APPROVED,        // Rank account created
    REJECTED,        // Declined with a reason
    WITHDRAWN;       // Cancelled by the applicant

    public static final Set<ApplicationStatus> OPEN = EnumSet.of(PENDING, INFO_REQUESTED);

    public boolean isOpen() {
        return OPEN.contains(this);
    }
}
