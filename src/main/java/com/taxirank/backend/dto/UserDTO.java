package com.taxirank.backend.dto;

import com.taxirank.backend.enums.UserRole;
import lombok.Data;

@Data
public class UserDTO {
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
    private UserRole role;
} 