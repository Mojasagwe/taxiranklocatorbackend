package com.taxirank.backend.dto;

import com.taxirank.backend.enums.AdminRequestStatus;
import lombok.Data;

/**
 * DTO for responding to a rank assignment request.
 */
@Data
public class RankAssignmentResponseDTO {
    private AdminRequestStatus status;
    private String responseMessage;
} 