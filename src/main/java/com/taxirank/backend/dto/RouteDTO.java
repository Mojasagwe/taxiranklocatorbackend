package com.taxirank.backend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.Duration;

@Data
public class RouteDTO {
    private Long id;
    private Long sourceRankId;
    private Long destinationRankId;
    private BigDecimal fare;
    private Duration estimatedDuration;
    private Double distance;
    private String trafficStatus;
    private String peakHours;
} 