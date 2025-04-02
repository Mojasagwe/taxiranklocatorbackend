package com.taxirank.backend.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taxirank.backend.dto.ApiResponse;
import com.taxirank.backend.models.TaxiRank;
import com.taxirank.backend.security.UserPrincipal;
import com.taxirank.backend.services.RankAdminService;
import com.taxirank.backend.services.TaxiRankService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private TaxiRankService taxiRankService;
    
    @Autowired
    private RankAdminService rankAdminService;
    
    /**
     * Get statistics for the SUPER_ADMIN dashboard
     * @return Map of statistics
     */
    @GetMapping("/admin-stats")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Get rank statistics
        stats.put("totalRanks", taxiRankService.getTotalRanksCount());
        stats.put("assignedRanks", taxiRankService.getAssignedRanksCount());
        stats.put("unassignedRanks", taxiRankService.getUnassignedRanks().size());
        
        return ResponseEntity.ok(ApiResponse.success("Dashboard statistics retrieved", stats));
    }
    
    /**
     * Get statistics for the current logged-in admin's dashboard
     * @param authentication The current authentication object
     * @return Map of statistics for the current admin
     */
    @GetMapping("/my-stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> getMyStats(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long currentUserId = userPrincipal.getId();
        
        Map<String, Object> stats = new HashMap<>();
        
        // Get ranks managed by this admin
        List<TaxiRank> managedRanks = rankAdminService.getRanksManagedByAdmin(currentUserId);
        stats.put("managedRanksCount", managedRanks.size());
        
        // Include simplified rank information
        List<Map<String, Object>> simplifiedRanks = managedRanks.stream()
            .map(rank -> {
                Map<String, Object> rankInfo = new HashMap<>();
                rankInfo.put("id", rank.getId());
                rankInfo.put("name", rank.getName());
                rankInfo.put("code", rank.getCode());
                rankInfo.put("city", rank.getCity());
                return rankInfo;
            })
            .collect(Collectors.toList());
        
        stats.put("managedRanks", simplifiedRanks);
        
        return ResponseEntity.ok(ApiResponse.success("Your dashboard statistics retrieved", stats));
    }
} 