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
import com.taxirank.backend.repositories.UserRepository;
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
    private UserRepository userRepository;
    
    @Autowired
    private TerminalService terminalService;
    
    @Autowired
    private TaxiRankMapper rankMapper;
    
    /**
     * Public endpoint to view all ranks
     */
    @GetMapping
    public ResponseEntity<?> getAllRanks(@RequestParam(required = false, defaultValue = "false") boolean includeStats) {
        try {
            List<TaxiRank> taxiRanks = taxiRankService.getAllRanks();
            List<TaxiRankDTO> dtos = rankMapper.toDTOList(taxiRanks, includeStats);
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
                                        @RequestParam(required = false, defaultValue = "false") boolean includeStats) {
        try {
            TaxiRank taxiRank = taxiRankService.getRankById(id);
            TaxiRankDTO dto = rankMapper.toDTO(taxiRank, includeStats);
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
            
            statistics.put("hasAdmin", userRepository.existsByRankId(id));
            
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
     * Create a new rank (System Administrators; ranks also come from approved applications)
     */
    @PostMapping
    @PreAuthorize("hasAuthority('RANKS_MANAGE')")
    public ResponseEntity<?> createRank(@Valid @RequestBody TaxiRankDTO taxiRankDTO) {
        try {
            TaxiRank taxiRank = rankMapper.createEntityFromDTO(taxiRankDTO);
            TaxiRank createdRank = taxiRankService.createRank(taxiRank);
            TaxiRankDTO createdDTO = rankMapper.toDTO(createdRank);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Taxi rank created successfully", createdDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create taxi rank: " + e.getMessage()));
        }
    }
    
    /**
     * Update a rank: any rank with RANKS_MANAGE, or the rank account's own rank
     */
    @PutMapping("/{id}")
    @PreAuthorize("@rankAccess.canManage(authentication, #id)")
    public ResponseEntity<?> updateRank(@PathVariable Long id, @Valid @RequestBody TaxiRankDTO taxiRankDTO) {
        try {
            TaxiRank updatedRank = taxiRankService.updateRank(id, rankMapper.createEntityFromDTO(taxiRankDTO));
            TaxiRankDTO updatedDTO = rankMapper.toDTO(updatedRank);
            
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
    @PreAuthorize("hasAuthority('RANKS_MANAGE')")
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
     * Activate or deactivate a rank (same rule as update)
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("@rankAccess.canManage(authentication, #id)")
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
            TaxiRankDTO updatedDTO = rankMapper.toDTO(updatedRank);
            
            return ResponseEntity.ok(ApiResponse.success(
                    "Taxi rank status updated successfully", updatedDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update taxi rank status: " + e.getMessage()));
        }
    }
    
}