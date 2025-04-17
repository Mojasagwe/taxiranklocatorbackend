package com.taxirank.backend.dto;

import java.util.List;
import java.util.Map;

import com.taxirank.backend.models.TaxiRank;
import lombok.Data;

/**
 * DTO for displaying rank admin overview in the dashboard
 */
@Data
public class RankAdminOverviewDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private List<TaxiRank> managedRanks;
    private int rankCount;
    private Map<Long, Map<String, Boolean>> rankPermissions;
    private Long totalTerminals;
    private Long activeTerminals;
} 