package com.taxirank.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taxirank.backend.dto.ApiResponse;
import com.taxirank.backend.dto.FilteredUserResponse;
import com.taxirank.backend.dto.RegisterRequest;
import com.taxirank.backend.dto.UserDetailsDTO;
import com.taxirank.backend.models.TaxiRank;
import com.taxirank.backend.models.User;
import com.taxirank.backend.security.JwtTokenProvider;
import com.taxirank.backend.services.AuthService;
import com.taxirank.backend.services.RankAdminService;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AuthService authService;
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private RankAdminService rankAdminService;
    
    // Rank admins join through admin-registration requests; only a super admin creates admins directly
    @PostMapping("/register")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> registerAdmin(@RequestBody RegisterRequest registerRequest) {
        try {
            User admin = authService.registerAdmin(registerRequest);
            String jwt = tokenProvider.generateToken(admin.getId());
            
            // Get user details with role-based filtering
            List<TaxiRank> managedRanks = rankAdminService.getRanksManagedByAdmin(admin.getId());
            UserDetailsDTO userDetailsDTO = UserDetailsDTO.fromUserWithRoleFilter(admin, managedRanks, admin.getRole());
            
            return ResponseEntity.ok(ApiResponse.success("Admin registered successfully", 
                new FilteredUserResponse(userDetailsDTO, jwt)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Admin registration failed: " + e.getMessage()));
        }
    }

    // Super admins are created only via POST /api/auth/register/super-admin (bootstrap token or existing super admin)
}