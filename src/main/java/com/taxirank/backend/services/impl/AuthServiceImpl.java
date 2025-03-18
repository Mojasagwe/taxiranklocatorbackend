package com.taxirank.backend.services.impl;

import com.taxirank.backend.dto.LoginRequest;
import com.taxirank.backend.dto.RegisterRequest;
import com.taxirank.backend.models.Rider;
import com.taxirank.backend.repositories.RiderRepository;
import com.taxirank.backend.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private RiderRepository riderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public Rider registerUser(RegisterRequest registerRequest) {
        if (riderRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Rider rider = new Rider();
        rider.setFirstName(registerRequest.getFirstName());
        rider.setLastName(registerRequest.getLastName());
        rider.setEmail(registerRequest.getEmail());
        rider.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        rider.setPhoneNumber(registerRequest.getPhoneNumber());
        rider.setPreferredPaymentMethod(registerRequest.getPreferredPaymentMethod());

        return riderRepository.save(rider);
    }

    @Override
    public Rider loginUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword()
            )
        );

        return riderRepository.findByEmail(loginRequest.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
} 