package com.taxirank.backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO for TaxiRank entities
 */
@Data
public class TaxiRankDTO {
    private Long id;
    
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;
    
    private String code; // Can be auto-generated
    
    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;
    
    @NotBlank(message = "Address is required")
    @Size(max = 200, message = "Address must be at most 200 characters")
    private String address;
    
    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must be at most 100 characters")
    private String city;
    
    @NotBlank(message = "Province is required")
    @Size(max = 100, message = "Province must be at most 100 characters")
    private String province;
    
    private Double latitude;
    
    private Double longitude;
    
    @Pattern(regexp = "^\\+?[0-9\\s-]{8,20}$", message = "Invalid phone number format")
    private String contactPhone;
    
    @Email(message = "Invalid email format")
    private String contactEmail;
    
    @Pattern(regexp = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]-([0-1]?[0-9]|2[0-3]):[0-5][0-9]$", 
            message = "Operating hours must be in the format 'HH:MM-HH:MM'")
    private String operatingHours;
    
    @NotNull(message = "Capacity is required")
    @Positive(message = "Capacity must be a positive number")
    private Integer capacity;
    
    private Boolean isActive = true;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Admin management
    private List<AdminSummaryDTO> admins;
    
    // Terminal summary
    private Long terminalCount;
    private Long activeTerminalCount;
    
    // Statistics
    private Map<String, Object> statistics;
    
    /**
     * Simplified admin information
     */
    @Data
    public static class AdminSummaryDTO {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
    }
} 