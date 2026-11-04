package com.taxirank.backend.enums;

/**
 * Permissions granted through profiles (see the profile_permissions table).
 * Code checks these with hasAuthority('...'), never profile names, so adding a
 * profile only needs a migration.
 */
public enum Permission {
    /** List and view any user account and its history */
    USERS_VIEW,
    /** Create users, edit them, change profiles and responsible persons */
    USERS_MANAGE,
    /** Issue a temporary password for another account */
    USERS_RESET_PASSWORD,
    /** Flag, suspend, block or reactivate another account */
    USERS_SET_STATUS,
    /** Review rank applications: approve, request info, link, reject */
    APPLICATIONS_REVIEW,
    /** Apply to manage a rank */
    APPLICATIONS_SUBMIT,
    /** Create, edit and delete any rank and its terminals */
    RANKS_MANAGE,
    /** Edit the rank this account belongs to, and its terminals */
    RANK_MANAGE_OWN,
    /** Create, edit and delete routes */
    ROUTES_MANAGE
}
