package com.taxirank.backend.dto;

import lombok.Data;

/**
 * DTO for creating a new rank assignment request.
 */
@Data
public class RankAssignmentRequestDTO {
    private Long rankId;
    private String rankCode; // Alternative to rankId
    private String requestReason;
} 