package com.taxirank.backend.controllers;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taxirank.backend.dto.ApiResponse;
import com.taxirank.backend.dto.TaxiRankDTO;
import com.taxirank.backend.models.TaxiRank;
import com.taxirank.backend.security.UserPrincipal;
import com.taxirank.backend.services.RankAdminService;
import com.taxirank.backend.services.TaxiRankService;
import com.taxirank.backend.services.TerminalService;
import com.taxirank.backend.utils.TaxiRankMapper;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/taxi-ranks")
public class TaxiRankController {

    @Autowired
    private TaxiRankService taxiRankService;
    
    @Autowired
    private RankAdminService rankAdminService;
    
    @Autowired
    private TerminalService terminalService;
    
    @Autowired
    private TaxiRankMapper rankMapper;
    
    /**
     * Public endpoint to view all ranks
     */
    @GetMapping
    public ResponseEntity<?> getAllRanks(@RequestParam(required = false, defaultValue = "false") boolean includeAdmins,
                                        @RequestParam(required = false, defaultValue = "false") boolean includeStats) {
        try {
            List<TaxiRank> taxiRanks = taxiRankService.getAllRanks();
            List<TaxiRankDTO> dtos = rankMapper.toDTOList(taxiRanks, includeAdmins, includeStats);
            return ResponseEntity.ok(ApiResponse.success("All taxi ranks retrieved successfully", dtos));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve taxi ranks: " + e.getMessage()));
        }
    }
    
    /**
     * Public endpoint to view a specific rank
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getRankById(@PathVariable Long id,
                                        @RequestParam(required = false, defaultValue = "false") boolean includeAdmins,
                                        @RequestParam(required = false, defaultValue = "false") boolean includeStats) {
        try {
            TaxiRank taxiRank = taxiRankService.getRankById(id);
            TaxiRankDTO dto = rankMapper.toDTO(taxiRank, includeAdmins, includeStats);
            return ResponseEntity.ok(ApiResponse.success("Taxi rank retrieved successfully", dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Taxi rank not found: " + e.getMessage()));
        }
    }
    
    /**
     * Get detailed statistics for a taxi rank
     */
    @GetMapping("/{id}/statistics")
    public ResponseEntity<?> getRankStatistics(@PathVariable Long id) {
        try {
            TaxiRank taxiRank = taxiRankService.getRankById(id);
            Map<String, Object> statistics = new HashMap<>();
            
            // Basic info
            statistics.put("rankId", taxiRank.getId());
            statistics.put("rankName", taxiRank.getName());
            statistics.put("rankCode", taxiRank.getCode());
            statistics.put("isActive", taxiRank.getIsActive());
            
            // Admin info - change from count to boolean
            int adminCount = rankAdminService.getAdminsForRank(id).size();
            statistics.put("hasAdmin", adminCount > 0);
            
            // Terminal statistics
            statistics.put("totalTerminals", terminalService.countTerminalsByRankId(id));
            statistics.put("activeTerminals", terminalService.countActiveTerminalsByRankId(id));
            
            // Add more statistics as needed
            
            return ResponseEntity.ok(ApiResponse.success("Rank statistics retrieved successfully", statistics));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Taxi rank not found: " + e.getMessage()));
        }
    }
    
    /**
     * Admin only - create a new rank
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> createRank(@Valid @RequestBody TaxiRankDTO taxiRankDTO) {
        try {
            TaxiRank taxiRank = rankMapper.createEntityFromDTO(taxiRankDTO);
            TaxiRank createdRank = taxiRankService.createRank(taxiRank);
            TaxiRankDTO createdDTO = rankMapper.toDTO(createdRank, false, false);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Taxi rank created successfully", createdDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create taxi rank: " + e.getMessage()));
        }
    }
    
    /**
     * Admin only - update a rank
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> updateRank(@PathVariable Long id, @Valid @RequestBody TaxiRankDTO taxiRankDTO) {
        try {
            // Check for rank admin permissions if applicable
            TaxiRank existingRank = taxiRankService.getRankById(id);
            TaxiRank updatedRank = taxiRankService.updateRank(id, rankMapper.createEntityFromDTO(taxiRankDTO));
            TaxiRankDTO updatedDTO = rankMapper.toDTO(updatedRank, false, false);
            
            return ResponseEntity.ok(ApiResponse.success("Taxi rank updated successfully", updatedDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update taxi rank: " + e.getMessage()));
        }
    }
    
    /**
     * Admin only - delete a rank
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteRank(@PathVariable Long id) {
        try {
            taxiRankService.deleteRank(id);
            return ResponseEntity.ok(ApiResponse.success("Taxi rank deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete taxi rank: " + e.getMessage()));
        }
    }
    
    /**
     * Admin only - activate or deactivate a rank
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> updateRankStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> status) {
        try {
            Boolean isActive = status.get("isActive");
            if (isActive == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("isActive field is required"));
            }
            
            TaxiRank taxiRank = taxiRankService.getRankById(id);
            taxiRank.setIsActive(isActive);
            TaxiRank updatedRank = taxiRankService.updateRank(id, taxiRank);
            TaxiRankDTO updatedDTO = rankMapper.toDTO(updatedRank, false, false);
            
            return ResponseEntity.ok(ApiResponse.success(
                    "Taxi rank status updated successfully", updatedDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update taxi rank status: " + e.getMessage()));
        }
    }
    
    /**
     * Check if the user has permission to manage a rank
     */
    private boolean hasRankAdminPermission(Authentication authentication, Long rankId) {
        // Super admins have access to all ranks
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
            return true;
        }
        
        // Check if the user is an admin for this rank
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();
        
        List<TaxiRank> managedRanks = rankAdminService.getRanksManagedByAdmin(userId);
        return managedRanks.stream().anyMatch(rank -> rank.getId().equals(rankId));
    }
}