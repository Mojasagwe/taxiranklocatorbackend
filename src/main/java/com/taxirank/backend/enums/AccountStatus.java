package com.taxirank.backend.enums;

/**
 * Enum representing the different states a user account can be in.
 */
public enum AccountStatus {
    /**
     * Account is active and can use all features
     */
    ACTIVE,
    
    /**
     * Account is inactive, typically user initiated
     */
    INACTIVE,
    
    /**
     * Account is temporarily suspended, typically admin initiated
     */
    SUSPENDED,
    
    /**
     * Account is permanently blocked from using the system
     */
    BLOCKED
} 