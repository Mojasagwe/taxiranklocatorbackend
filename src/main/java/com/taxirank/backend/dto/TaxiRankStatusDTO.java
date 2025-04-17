package com.taxirank.backend.dto;

import com.taxirank.backend.models.User;
import lombok.Data;

import java.util.List;

/**
 * DTO for displaying taxi rank status in the dashboard
 */
@Data
public class TaxiRankStatusDTO {
    private Long id;
    private String name;
    private String code;
    private String city;
    private String province;
    private Boolean isAssigned;
    private List<User> assignedAdmins;
    private Long terminalCount;
    private Long activeTerminalCount;
} 