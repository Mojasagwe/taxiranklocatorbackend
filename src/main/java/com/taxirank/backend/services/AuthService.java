package com.taxirank.backend.services;

import com.taxirank.backend.dto.LoginRequest;
import com.taxirank.backend.dto.RegisterRequest;
import com.taxirank.backend.models.User;

public interface AuthService {
    User registerUser(RegisterRequest registerRequest);
    User loginUser(LoginRequest loginRequest);
    User registerAdmin(RegisterRequest registerRequest);
} 