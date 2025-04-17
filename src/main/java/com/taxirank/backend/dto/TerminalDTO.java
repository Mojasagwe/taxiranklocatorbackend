package com.taxirank.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.util.List;

/**
 * DTO for Terminal entities
 */
@Data
public class TerminalDTO {
    private Long id;
    
    @NotBlank(message = "Destination name is required")
    private String name;
    
    @NotNull(message = "Fare is required")
    @Positive(message = "Fare must be positive")
    private Double fare;
    
    @NotBlank(message = "Travel time is required")
    private String travelTime;
    
    @NotBlank(message = "Distance is required")
    private String distance;
    
    private String departureSchedule;
    
    private List<String> operatingDays;
    
    private Boolean isActive = true;
    
    // For creating/updating a terminal
    private Long taxiRankId;
    
    // For read operations
    private String taxiRankName;
    private String taxiRankCode;
} 