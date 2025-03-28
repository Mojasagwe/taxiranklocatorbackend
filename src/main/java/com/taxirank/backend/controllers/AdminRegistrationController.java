package com.taxirank.backend.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taxirank.backend.dto.AdminReviewRequest;
import com.taxirank.backend.dto.ApiResponse;
import com.taxirank.backend.enums.AdminRequestStatus;
import com.taxirank.backend.models.TaxiRank;
import com.taxirank.backend.security.UserPrincipal;
import com.taxirank.backend.services.AdminRegistrationService;
import com.taxirank.backend.services.TaxiRankService;

@RestController
@RequestMapping("/api/admin-registration")
public class AdminRegistrationController {

    @Autowired
    private AdminRegistrationService adminRegistrationService;
    
    @Autowired
    private TaxiRankService taxiRankService;
    
    // Public endpoint to get available ranks for registration
    @GetMapping("/available-ranks")
    public ResponseEntity<?> getAvailableRanks() {
        List<TaxiRank> ranks = taxiRankService.getAllRanks();
        return ResponseEntity.ok(ApiResponse.success("Available taxi ranks retrieved", ranks));
    }
    
    // Public endpoint for self-registration
    @PostMapping("/request")
    public ResponseEntity<?> submitRegistrationRequest(@RequestBody com.taxirank.backend.dto.AdminRegistrationRequest requestDTO) {
        try {
            // Validate selected ranks before submission
            if (requestDTO.getSelectedRankIds() != null && !requestDTO.getSelectedRankIds().isEmpty()) {
                List<TaxiRank> ranks = taxiRankService.getAllRanks();
                
                // Filter to only the selected ranks
                List<TaxiRank> selectedRanks = ranks.stream()
                    .filter(rank -> requestDTO.getSelectedRankIds().contains(rank.getId()))
                    .collect(Collectors.toList());
                
                // Check if any of the selected ranks already have admins
                for (TaxiRank rank : selectedRanks) {
                    if (adminRegistrationService.isRankAlreadyAssigned(rank.getId())) {
                        return ResponseEntity.badRequest().body(
                            ApiResponse.error("Registration request failed: Rank '" + rank.getName() 
                            + "' already has an admin assigned to it. Please select a different rank.")
                        );
                    }
                }
            }
            
            com.taxirank.backend.models.AdminRegistrationRequest request = adminRegistrationService.submitRequest(requestDTO);
            return ResponseEntity.ok(ApiResponse.success("Admin registration request submitted successfully. Awaiting approval.", request.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Registration request failed: " + e.getMessage()));
        }
    }
    
    // Admin only - get all pending requests
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPendingRequests() {
        List<com.taxirank.backend.models.AdminRegistrationRequest> requests = adminRegistrationService.getPendingRequests();
        return ResponseEntity.ok(ApiResponse.success("Pending admin registration requests retrieved", requests));
    }
    
    // Admin only - get all requests by status
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getRequestsByStatus(@PathVariable AdminRequestStatus status) {
        List<com.taxirank.backend.models.AdminRegistrationRequest> requests = adminRegistrationService.getRequestsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success("Admin registration requests with status " + status + " retrieved", requests));
    }
    
    // Admin only - get specific request
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getRequestById(@PathVariable Long id) {
        try {
            com.taxirank.backend.models.AdminRegistrationRequest request = adminRegistrationService.getRequestById(id);
            return ResponseEntity.ok(ApiResponse.success("Admin registration request retrieved", request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve request: " + e.getMessage()));
        }
    }
    
    // Admin only - review request
    @PutMapping("/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> reviewRequest(
            @PathVariable Long id,
            @RequestBody AdminReviewRequest reviewDTO,
            Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Long reviewerId = userPrincipal.getId();
            
            com.taxirank.backend.models.AdminRegistrationRequest request = 
                    adminRegistrationService.reviewRequest(id, reviewDTO, reviewerId);
            
            String message = reviewDTO.getStatus() == AdminRequestStatus.APPROVED
                    ? "Registration request approved successfully"
                    : "Registration request rejected";
                    
            return ResponseEntity.ok(ApiResponse.success(message, request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to process request: " + e.getMessage()));
        }
    }
}