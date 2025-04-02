package com.taxirank.backend.enums;

/**
 * Enum representing the different roles a user can have in the system.
 * Used for role-based access control.
 */
public enum UserRole {
    /**
     * Regular user who can book rides and view taxi ranks
     */
    RIDER,
    
    /**
     * Administrative user who can manage taxi ranks, routes, and user accounts
     */
    ADMIN,
    
    /**
     * Super administrator with highest level of system access
     * Can manage other administrators and has unrestricted access
     */
    SUPER_ADMIN
} 