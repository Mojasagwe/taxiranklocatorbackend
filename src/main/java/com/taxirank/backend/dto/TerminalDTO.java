package com.taxirank.backend.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalTime;
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
    
    // Where the route ends (map pin from Google place search) and, optionally, the rank it ends at
    @DecimalMin("-90") @DecimalMax("90")
    private Double destinationLatitude;

    @DecimalMin("-180") @DecimalMax("180")
    private Double destinationLongitude;

    @Size(max = 255)
    private String destinationPlaceId;

    private Long endsAtRankId;

    @Size(max = 50)
    private String bay;

    @Min(1) @Max(240)
    private Integer intervalMinutes;

    // false = paused for today only; it runs again tomorrow
    private Boolean runningToday;

    private LocalTime firstTaxi;
    private LocalTime lastTaxiWeekday;
    private LocalTime lastTaxiSaturday;
    private LocalTime lastTaxiSunday;

    // For read operations
    private String taxiRankName;
    private String taxiRankCode;
    private String endsAtRankName;
    // Riders reporting a different fare since the rank last confirmed it (rank admin view)
    private RouteDtos.FareReport fareReport;
} 