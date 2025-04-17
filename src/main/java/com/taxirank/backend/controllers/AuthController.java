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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> registerSuperAdmin(@RequestBody RegisterRequest registerRequest) {
        try {
            // Check if there are any existing super admins
            if (userService.getUsersByRole(UserRole.SUPER_ADMIN).isEmpty()) {
                // This is the first super admin, allow it
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