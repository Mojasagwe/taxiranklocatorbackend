package com.taxirank.backend.dto;

import lombok.Data;

/**
 * DTO for assigning a taxi rank to an admin user.
 */
@Data
public class RankAdminAssignmentDTO {
    private Long userId;
    private String rankCode;
    private String designation;
    private String notes;
    
    // Permission fields
    private Boolean canManageDrivers;
    private Boolean canViewFinancials;
    private Boolean canEditRankDetails;
    private Boolean canManageRoutes;
    private Boolean canManageTerminals;
} 