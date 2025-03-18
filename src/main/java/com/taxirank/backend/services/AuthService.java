package com.taxirank.backend.services;

import com.taxirank.backend.dto.LoginRequest;
import com.taxirank.backend.dto.RegisterRequest;
import com.taxirank.backend.models.Rider;

public interface AuthService {
    Rider registerUser(RegisterRequest registerRequest);
    Rider loginUser(LoginRequest loginRequest);
} 