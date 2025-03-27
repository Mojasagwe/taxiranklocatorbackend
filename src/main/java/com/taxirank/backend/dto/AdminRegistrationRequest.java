package com.taxirank.backend.dto;

import com.taxirank.backend.enums.PaymentMethod;
import lombok.Data;

import java.util.List;

/**
 * DTO for admin self-registration with rank selection.
 */
@Data
public class AdminRegistrationRequest {
    // User information
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phoneNumber;
    private PaymentMethod preferredPaymentMethod;
    
    // Admin specific information
    private List<Long> selectedRankIds;
    private String designation;
    private String justification;
    private String professionalExperience;
    private String adminNotes;
}