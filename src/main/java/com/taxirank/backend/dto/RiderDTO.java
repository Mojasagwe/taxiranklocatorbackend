package com.taxirank.backend.dto;

import lombok.Data;

@Data
public class RiderDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String password;
    private String profilePicture;
    private String preferredPaymentMethod;
    private String accountStatus;
    private Boolean isVerified;
    private Double rating;
    private Integer totalTrips;
} 