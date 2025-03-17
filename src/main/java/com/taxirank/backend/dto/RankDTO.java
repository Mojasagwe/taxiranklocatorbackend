package com.taxirank.backend.dto;

import lombok.Data;

@Data
public class RankDTO {
    private Long id;
    private String name;
    private String location;
    private Double latitude;
    private Double longitude;
    private String contactNumber;
    private String operatingHours;
    private String status;
    private Integer capacity;
    private Double averageWaitTime;
} 