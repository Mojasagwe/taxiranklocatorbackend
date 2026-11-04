package com.taxirank.backend.services;

import com.taxirank.backend.dto.LoginRequest;
import com.taxirank.backend.dto.RegisterRequest;
import com.taxirank.backend.models.User;

public interface AuthService {
    /** Self sign-up: riders and rank admin applicants alike get the Commuter profile */
    User registerCommuter(RegisterRequest registerRequest);

    /** Throws BadCredentialsException or LockedException; records the login time */
    User login(LoginRequest loginRequest);

    /** Creates the first System Administrator; fails once one exists */
    User bootstrapSystemAdmin(RegisterRequest registerRequest);

    /** Also clears the must-change-password flag after an admin-issued password */
    void changePassword(Long userId, String currentPassword, String newPassword);
}
