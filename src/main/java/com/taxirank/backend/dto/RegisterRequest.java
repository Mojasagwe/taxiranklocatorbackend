package com.taxirank.backend.dto;

import com.taxirank.backend.enums.PaymentMethod;
import lombok.Data;

@Data
public class RegisterRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phoneNumber;
    private PaymentMethod preferredPaymentMethod;
} 