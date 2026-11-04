package com.taxirank.backend.enums;

/**
 * Enum representing the different states a user account can be in.
 * Anything other than ACTIVE needs a reason (users.status_reason).
 */
public enum AccountStatus {
    /**
     * Account is active and can use all features
     */
    ACTIVE,

    /**
     * Account still works but is marked for review by a System Administrator
     */
    FLAGGED,

    /**
     * Account cannot log in until reactivated
     */
    SUSPENDED,

    /**
     * Account is permanently locked
     */
    BLOCKED,

    /**
     * The person deleted their account; personal data is erased and it can never log in again
     */
    DELETED;

    public boolean canLogIn() {
        return this == ACTIVE || this == FLAGGED;
    }
}
