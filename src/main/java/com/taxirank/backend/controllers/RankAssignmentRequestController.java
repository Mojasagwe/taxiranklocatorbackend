package com.taxirank.backend.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taxirank.backend.dto.ApiResponse;
import com.taxirank.backend.dto.RankAssignmentRequestDTO;
import com.taxirank.backend.dto.RankAssignmentResponseDTO;
import com.taxirank.backend.enums.AdminRequestStatus;
import com.taxirank.backend.enums.UserRole;
import com.taxirank.backend.models.RankAssignmentRequest;
import com.taxirank.backend.security.UserPrincipal;
import com.taxirank.backend.services.RankAssignmentRequestService;

@RestController
@RequestMapping("/api/rank-assignment-requests")
public class RankAssignmentRequestController {

    @Autowired
    private RankAssignmentRequestService rankAssignmentRequestService;
    
    /**
     * Submit a new rank assignment request
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> submitRequest(
            @RequestBody RankAssignmentRequestDTO requestDTO,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            RankAssignmentRequest request = rankAssignmentRequestService.submitRequest(requestDTO, currentUser.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Rank assignment request submitted successfully", request));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to submit request: " + e.getMessage()));
        }
    }
    
    /**
     * Get all requests by status (admins can view their own, super admins can view all)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> getRequestsByStatus(
            @RequestParam(required = false) AdminRequestStatus status,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            List<RankAssignmentRequest> requests;
            
            if (currentUser.getRole() == UserRole.ADMIN) {
                // Regular admins can only view their own requests
                requests = rankAssignmentRequestService.getRequestsByAdmin(currentUser.getId());
                if (status != null) {
                    requests = requests.stream()
                            .filter(req -> req.getStatus() == status)
                            .toList();
                }
            } else {
                // Super admins can view all requests
                if (status != null) {
                    requests = rankAssignmentRequestService.getRequestsByStatus(status);
                } else {
                    requests = rankAssignmentRequestService.getRequestsByStatus(null);
                }
            }
            
            return ResponseEntity.ok(ApiResponse.success("Requests retrieved successfully", requests));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve requests: " + e.getMessage()));
        }
    }
    
    /**
     * Get a specific request by ID
     */
    @GetMapping("/{requestId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> getRequestById(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            RankAssignmentRequest request = rankAssignmentRequestService.getRequestById(requestId);
            
            // Check authorization - only super admins or the requesting admin can view
            if (currentUser.getRole() != UserRole.SUPER_ADMIN && 
                    !request.getRequestingAdmin().getUser().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("You are not authorized to view this request"));
            }
            
            return ResponseEntity.ok(ApiResponse.success("Request retrieved successfully", request));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve request: " + e.getMessage()));
        }
    }
    
    /**
     * Review and respond to a request (super admin only)
     */
    @PutMapping("/{requestId}/review")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> reviewRequest(
            @PathVariable Long requestId,
            @RequestBody RankAssignmentResponseDTO responseDTO,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            RankAssignmentRequest request = rankAssignmentRequestService.reviewRequest(
                    requestId, responseDTO, currentUser.getId());
            
            return ResponseEntity.ok(ApiResponse.success(
                    "Request " + responseDTO.getStatus().toString().toLowerCase() + " successfully", request));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to review request: " + e.getMessage()));
        }
    }
    
    /**
     * Cancel a request (by the requesting admin only)
     */
    @PutMapping("/{requestId}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cancelRequest(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            RankAssignmentRequest request = rankAssignmentRequestService.cancelRequest(requestId, currentUser.getId());
            
            return ResponseEntity.ok(ApiResponse.success("Request cancelled successfully", request));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to cancel request: " + e.getMessage()));
        }
    }
    
    /**
     * Get all requests for a specific rank (super admin only)
     */
    @GetMapping("/ranks/{rankId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getRequestsByRank(@PathVariable Long rankId) {
        try {
            List<RankAssignmentRequest> requests = rankAssignmentRequestService.getRequestsByRank(rankId);
            
            return ResponseEntity.ok(ApiResponse.success("Requests retrieved successfully", requests));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve requests: " + e.getMessage()));
        }
    }
    
    /**
     * Get all requests for a specific rank by code (super admin only)
     */
    @GetMapping("/ranks/code/{rankCode}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getRequestsByRankCode(@PathVariable String rankCode) {
        try {
            List<RankAssignmentRequest> requests = rankAssignmentRequestService.getRequestsByRankCode(rankCode);
            
            return ResponseEntity.ok(ApiResponse.success("Requests retrieved successfully", requests));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve requests: " + e.getMessage()));
        }
    }
    
    /**
     * Check if a rank has any pending requests (super admin only)
     */
    @GetMapping("/ranks/{rankId}/has-pending")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> hasPendingRequestsForRank(@PathVariable Long rankId) {
        try {
            boolean hasPending = rankAssignmentRequestService.hasPendingRequestsForRank(rankId);
            
            return ResponseEntity.ok(ApiResponse.success("Status retrieved successfully", hasPending));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to check status: " + e.getMessage()));
        }
    }
    
    /**
     * Check if current admin has a pending request for a specific rank
     */
    @GetMapping("/check-pending/{rankId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> hasCurrentAdminPendingRequestForRank(
            @PathVariable Long rankId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            boolean hasPending = rankAssignmentRequestService.hasAdminPendingRequestForRank(
                    currentUser.getId(), rankId);
            
            return ResponseEntity.ok(ApiResponse.success("Status retrieved successfully", hasPending));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to check status: " + e.getMessage()));
        }
    }
    
    /**
     * Check if current admin has a pending request for a specific rank by code
     */
    @GetMapping("/check-pending/code/{rankCode}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> hasCurrentAdminPendingRequestForRankByCode(
            @PathVariable String rankCode,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            boolean hasPending = rankAssignmentRequestService.hasAdminPendingRequestForRankByCode(
                    currentUser.getId(), rankCode);
            
            return ResponseEntity.ok(ApiResponse.success("Status retrieved successfully", hasPending));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to check status: " + e.getMessage()));
        }
    }

    /**
     * Get all requests for a specific admin (super admin only)
     */
    @GetMapping("/admin/{adminId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getRequestsByAdmin(@PathVariable Long adminId) {
        try {
            List<RankAssignmentRequest> requests = rankAssignmentRequestService.getRequestsByAdmin(adminId);
            
            return ResponseEntity.ok(ApiResponse.success("Requests retrieved successfully", requests));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve requests: " + e.getMessage()));
        }
    }
} 