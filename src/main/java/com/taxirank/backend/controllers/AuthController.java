package com.taxirank.backend.controllers;

import com.taxirank.backend.dto.ApiResponse;
import com.taxirank.backend.dto.FilteredUserResponse;
import com.taxirank.backend.dto.LoginRequest;
import com.taxirank.backend.dto.RegisterRequest;
import com.taxirank.backend.dto.UserDetailsDTO;
import com.taxirank.backend.enums.UserRole;
import com.taxirank.backend.models.User;
import com.taxirank.backend.security.JwtTokenProvider;
import com.taxirank.backend.services.AuthService;
import com.taxirank.backend.services.RankAdminService;
import com.taxirank.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserService userService;
    
    @Autowired
    private RankAdminService rankAdminService;

    // Empty (the default) disables first-super-admin bootstrap entirely
    @Value("${app.bootstrap.super-admin-token:}")
    private String superAdminBootstrapToken;

    private boolean isValidBootstrapToken(String provided) {
        if (!StringUtils.hasText(superAdminBootstrapToken) || provided == null) {
            return false;
        }
        return MessageDigest.isEqual(
            superAdminBootstrapToken.getBytes(StandardCharsets.UTF_8),
            provided.getBytes(StandardCharsets.UTF_8));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        try {
            User user = authService.registerUser(registerRequest);
            String jwt = tokenProvider.generateToken(user.getId());
            
            // Get user details with role-based filtering
            List<com.taxirank.backend.models.TaxiRank> managedRanks = rankAdminService.getRanksManagedByAdmin(user.getId());
            UserDetailsDTO userDetailsDTO = UserDetailsDTO.fromUserWithRoleFilter(user, managedRanks, user.getRole());
            
            return ResponseEntity.ok(ApiResponse.success("User registered successfully", 
                new FilteredUserResponse(userDetailsDTO, jwt)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Registration failed: " + e.getMessage()));
        }
    }
    
    @PostMapping("/register/super-admin")
    public ResponseEntity<?> registerSuperAdmin(@RequestBody RegisterRequest registerRequest,
            @RequestHeader(value = "X-Bootstrap-Token", required = false) String bootstrapToken) {
        try {
            // Check if there are any existing super admins
            if (userService.getUsersByRole(UserRole.SUPER_ADMIN).isEmpty()) {
                // The first super admin needs the bootstrap token, so nobody can race the owner on a fresh deploy
                if (!isValidBootstrapToken(bootstrapToken)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("A valid bootstrap token is required to create the first super admin"));
                }
                User user = authService.registerSuperAdmin(registerRequest);
                String jwt = tokenProvider.generateToken(user.getId());
                
                // Get user details with role-based filtering
                List<com.taxirank.backend.models.TaxiRank> managedRanks = rankAdminService.getRanksManagedByAdmin(user.getId());
                UserDetailsDTO userDetailsDTO = UserDetailsDTO.fromUserWithRoleFilter(user, managedRanks, user.getRole());
                
                return ResponseEntity.ok(ApiResponse.success("First Super Admin registered successfully", 
                    new FilteredUserResponse(userDetailsDTO, jwt)));
            } else {
                // For subsequent super admin registrations, check if the requester is a super admin
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                
                if (authentication != null && 
                    authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
                    
                    User user = authService.registerSuperAdmin(registerRequest);
                    String jwt = tokenProvider.generateToken(user.getId());
                    
                    // Get user details with role-based filtering
                    List<com.taxirank.backend.models.TaxiRank> managedRanks = rankAdminService.getRanksManagedByAdmin(user.getId());
                    UserDetailsDTO userDetailsDTO = UserDetailsDTO.fromUserWithRoleFilter(user, managedRanks, user.getRole());
                    
                    return ResponseEntity.ok(ApiResponse.success("Super Admin registered successfully", 
                        new FilteredUserResponse(userDetailsDTO, jwt)));
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("Only existing super admins can create new super admins"));
                }
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Super Admin registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            User user = authService.loginUser(loginRequest);
            String jwt = tokenProvider.generateToken(user.getId());
            
            // Get user details with role-based filtering
            List<com.taxirank.backend.models.TaxiRank> managedRanks = rankAdminService.getRanksManagedByAdmin(user.getId());
            UserDetailsDTO userDetailsDTO = UserDetailsDTO.fromUserWithRoleFilter(user, managedRanks, user.getRole());
            
            return ResponseEntity.ok(ApiResponse.success("Login successful", 
                new FilteredUserResponse(userDetailsDTO, jwt)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Login failed: " + e.getMessage()));
        }
    }
} 