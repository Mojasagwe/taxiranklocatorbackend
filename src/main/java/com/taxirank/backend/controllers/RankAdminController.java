package com.taxirank.backend.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taxirank.backend.dto.ApiResponse;
import com.taxirank.backend.dto.RankAdminAssignmentDTO;
import com.taxirank.backend.models.RankAdmin;
import com.taxirank.backend.models.TaxiRank;
import com.taxirank.backend.models.User;
import com.taxirank.backend.services.RankAdminService;

@RestController
@RequestMapping("/api/rank-admins")
@PreAuthorize("hasRole('ADMIN')")
public class RankAdminController {

    @Autowired
    private RankAdminService rankAdminService;
    
    @PostMapping
    public ResponseEntity<?> assignAdminToRank(@RequestBody RankAdminAssignmentDTO assignmentDTO) {
        try {
            RankAdmin rankAdmin = rankAdminService.assignAdminToRank(assignmentDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Admin assigned to rank successfully", rankAdmin));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to assign admin: " + e.getMessage()));
        }
    }
    
    @GetMapping("/users/{userId}/ranks")
    public ResponseEntity<?> getRanksManagedByAdmin(@PathVariable Long userId) {
        try {
            List<TaxiRank> ranks = rankAdminService.getRanksManagedByAdmin(userId);
            return ResponseEntity.ok(ApiResponse.success("Ranks retrieved successfully", ranks));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get ranks: " + e.getMessage()));
        }
    }
    
    @GetMapping("/ranks/{rankId}/admins")
    public ResponseEntity<?> getAdminsForRank(@PathVariable Long rankId) {
        try {
            List<User> admins = rankAdminService.getAdminsForRank(rankId);
            return ResponseEntity.ok(ApiResponse.success("Admins retrieved successfully", admins));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get admins: " + e.getMessage()));
        }
    }
    
    @GetMapping("/users/{userId}/ranks/{rankId}")
    public ResponseEntity<?> getAdminRankAssignment(@PathVariable Long userId, @PathVariable Long rankId) {
        try {
            RankAdmin assignment = rankAdminService.getAdminRankAssignment(userId, rankId);
            return ResponseEntity.ok(ApiResponse.success("Assignment retrieved successfully", assignment));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get assignment: " + e.getMessage()));
        }
    }
    
    @PutMapping("/users/{userId}/ranks/{rankId}")
    public ResponseEntity<?> updateAdminPermissions(
            @PathVariable Long userId, 
            @PathVariable Long rankId, 
            @RequestBody RankAdminAssignmentDTO updateDTO) {
        try {
            RankAdmin updatedAssignment = rankAdminService.updateAdminPermissions(userId, rankId, updateDTO);
            return ResponseEntity.ok(ApiResponse.success("Permissions updated successfully", updatedAssignment));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update permissions: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/users/{userId}/ranks/{rankId}")
    public ResponseEntity<?> removeAdminFromRank(@PathVariable Long userId, @PathVariable Long rankId) {
        try {
            rankAdminService.removeAdminFromRank(userId, rankId);
            return ResponseEntity.ok(ApiResponse.success("Admin removed from rank successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to remove admin: " + e.getMessage()));
        }
    }
} 