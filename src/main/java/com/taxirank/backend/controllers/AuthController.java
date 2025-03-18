package com.taxirank.backend.controllers;

import com.taxirank.backend.dto.ApiResponse;
import com.taxirank.backend.dto.LoginRequest;
import com.taxirank.backend.dto.RegisterRequest;
import com.taxirank.backend.models.Rider;
import com.taxirank.backend.security.JwtTokenProvider;
import com.taxirank.backend.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        try {
            Rider rider = authService.registerUser(registerRequest);
            String jwt = tokenProvider.generateToken(rider.getId());
            return ResponseEntity.ok(ApiResponse.success("User registered successfully", 
                new RegisterResponse(rider, jwt)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            Rider rider = authService.loginUser(loginRequest);
            String jwt = tokenProvider.generateToken(rider.getId());
            return ResponseEntity.ok(ApiResponse.success("Login successful", 
                new LoginResponse(rider, jwt)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Login failed: " + e.getMessage()));
        }
    }
}

class RegisterResponse {
    private final Rider rider;
    private final String token;

    public RegisterResponse(Rider rider, String token) {
        this.rider = rider;
        this.token = token;
    }

    public Rider getRider() {
        return rider;
    }

    public String getToken() {
        return token;
    }
}

class LoginResponse {
    private final Rider rider;
    private final String token;

    public LoginResponse(Rider rider, String token) {
        this.rider = rider;
        this.token = token;
    }

    public Rider getRider() {
        return rider;
    }

    public String getToken() {
        return token;
    }
} 