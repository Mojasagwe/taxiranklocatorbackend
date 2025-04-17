package com.taxirank.backend.services.impl;

import com.taxirank.backend.dto.LoginRequest;
import com.taxirank.backend.dto.RegisterRequest;
import com.taxirank.backend.enums.UserRole;
import com.taxirank.backend.models.User;
import com.taxirank.backend.repositories.UserRepository;
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
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public User registerUser(RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setRole(UserRole.RIDER); // explicitly set default role

        return userRepository.save(user);
    }
    
    @Override
    @Transactional
    public User registerAdmin(RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setRole(UserRole.ADMIN); // set admin role

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User registerSuperAdmin(RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setRole(UserRole.SUPER_ADMIN); // set super admin role

        return userRepository.save(user);
    }

    @Override
    public User loginUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword()
            )
        );

        return userRepository.findByEmail(loginRequest.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
} 