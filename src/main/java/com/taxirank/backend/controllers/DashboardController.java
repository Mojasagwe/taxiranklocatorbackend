package com.taxirank.backend.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taxirank.backend.dto.ApiResponse;
import com.taxirank.backend.dto.RankAdminOverviewDTO;
import com.taxirank.backend.dto.TaxiRankStatusDTO;
import com.taxirank.backend.enums.UserRole;
import com.taxirank.backend.models.RankAdmin;
import com.taxirank.backend.models.TaxiRank;
import com.taxirank.backend.models.User;
import com.taxirank.backend.repositories.RankAdminRepository;
import com.taxirank.backend.security.UserPrincipal;
import com.taxirank.backend.services.RankAdminService;
import com.taxirank.backend.services.TaxiRankService;
import com.taxirank.backend.services.TerminalService;
import com.taxirank.backend.services.UserService;

@RestController
public class DashboardController {

    @Autowired
    private TaxiRankService taxiRankService;
    
    @Autowired
    private RankAdminService rankAdminService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private RankAdminRepository rankAdminRepository;
    
    @Autowired
    private TerminalService terminalService;
    
    /**
     * SUPER ADMIN DASHBOARD
     */
    @RestController
    @RequestMapping("/api/dashboard/super-admin")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public class SuperAdminDashboardController {
        /**
         * Get statistics for the SUPER_ADMIN dashboard
         * @return Map of statistics
         */
        @GetMapping("/stats")
        public ResponseEntity<?> getAdminStats() {
            Map<String, Object> stats = new HashMap<>();
            
            // Get rank statistics
            stats.put("totalRanks", taxiRankService.getTotalRanksCount());
            stats.put("assignedRanks", taxiRankService.getAssignedRanksCount());
            stats.put("unassignedRanks", taxiRankService.getUnassignedRanks().size());
            
            // Get terminal statistics across all ranks
            long totalTerminals = 0;
            long activeTerminals = 0;
            List<TaxiRank> allRanks = taxiRankService.getAllRanks();
            for (TaxiRank rank : allRanks) {
                totalTerminals += terminalService.countTerminalsByRankId(rank.getId());
                activeTerminals += terminalService.countActiveTerminalsByRankId(rank.getId());
            }
            stats.put("totalTerminals", totalTerminals);
            stats.put("activeTerminals", activeTerminals);
            
            return ResponseEntity.ok(ApiResponse.success("Dashboard statistics retrieved", stats));
        }
        
        /**
         * Get all rank admins with their assigned ranks
         * @return List of rank admins with their assigned ranks
         */
        @GetMapping("/rank-admins")
        public ResponseEntity<?> getAllRankAdmins() {
            List<User> admins = userService.getUsersByRole(UserRole.ADMIN);
            List<RankAdminOverviewDTO> adminOverviews = new ArrayList<>();
            
            for (User admin : admins) {
                RankAdminOverviewDTO overview = new RankAdminOverviewDTO();
                overview.setId(admin.getId());
                overview.setFirstName(admin.getFirstName());
                overview.setLastName(admin.getLastName());
                overview.setEmail(admin.getEmail());
                overview.setPhoneNumber(admin.getPhoneNumber());
                
                // Get ranks managed by this admin
                List<TaxiRank> managedRanks = rankAdminService.getRanksManagedByAdmin(admin.getId());
                overview.setManagedRanks(managedRanks);
                overview.setRankCount(managedRanks.size());
                
                adminOverviews.add(overview);
            }
            
            return ResponseEntity.ok(ApiResponse.success("All rank admins retrieved", adminOverviews));
        }
        
        /**
         * Get the status of all taxi ranks (assigned/unassigned)
         * @return List of all taxi ranks with their admin assignment status
         */
        @GetMapping("/ranks-status")
        public ResponseEntity<?> getAllRanksStatus() {
            List<TaxiRank> allRanks = taxiRankService.getAllRanks();
            List<TaxiRankStatusDTO> rankStatusList = new ArrayList<>();
            
            for (TaxiRank rank : allRanks) {
                TaxiRankStatusDTO statusDTO = new TaxiRankStatusDTO();
                statusDTO.setId(rank.getId());
                statusDTO.setName(rank.getName());
                statusDTO.setCode(rank.getCode());
                statusDTO.setCity(rank.getCity());
                statusDTO.setProvince(rank.getProvince());
                
                // Check if rank has admin assigned
                List<User> assignedAdmins = rankAdminService.getAdminsForRank(rank.getId());
                statusDTO.setIsAssigned(!assignedAdmins.isEmpty());
                statusDTO.setAssignedAdmins(assignedAdmins);
                
                // Add terminal statistics
                statusDTO.setTerminalCount(terminalService.countTerminalsByRankId(rank.getId()));
                statusDTO.setActiveTerminalCount(terminalService.countActiveTerminalsByRankId(rank.getId()));
                
                rankStatusList.add(statusDTO);
            }
            
            return ResponseEntity.ok(ApiResponse.success("All ranks status retrieved", rankStatusList));
        }
        
        /**
         * Get details about a specific rank admin and their assigned ranks
         * @param adminId The ID of the admin
         * @return Details about the admin and their assigned ranks
         */
        @GetMapping("/rank-admins/{adminId}")
        public ResponseEntity<?> getRankAdminDetails(@PathVariable Long adminId) {
            try {
                User admin = userService.getUserById(adminId);
                
                if (admin.getRole() != UserRole.ADMIN) {
                    return ResponseEntity.badRequest()
                        .body(ApiResponse.error("User is not a rank admin"));
                }
                
                RankAdminOverviewDTO overview = new RankAdminOverviewDTO();
                overview.setId(admin.getId());
                overview.setFirstName(admin.getFirstName());
                overview.setLastName(admin.getLastName());
                overview.setEmail(admin.getEmail());
                overview.setPhoneNumber(admin.getPhoneNumber());
                
                // Get ranks managed by this admin
                List<TaxiRank> managedRanks = rankAdminService.getRanksManagedByAdmin(admin.getId());
                overview.setManagedRanks(managedRanks);
                overview.setRankCount(managedRanks.size());
                
                // Get the rank admin assignments to get permission details
                List<RankAdmin> assignments = rankAdminRepository.findByUser(admin);
                Map<Long, Map<String, Boolean>> rankPermissions = new HashMap<>();
                
                for (RankAdmin assignment : assignments) {
                    Map<String, Boolean> permissions = new HashMap<>();
                    permissions.put("canManageDrivers", assignment.isCanManageDrivers());
                    permissions.put("canViewFinancials", assignment.isCanViewFinancials());
                    permissions.put("canEditRankDetails", assignment.isCanEditRankDetails());
                    permissions.put("canManageRoutes", assignment.isCanManageRoutes());
                    permissions.put("canManageTerminals", assignment.isCanManageTerminals());
                    
                    rankPermissions.put(assignment.getTaxiRank().getId(), permissions);
                }
                
                overview.setRankPermissions(rankPermissions);
                
                return ResponseEntity.ok(ApiResponse.success("Rank admin details retrieved", overview));
            } catch (Exception e) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve rank admin details: " + e.getMessage()));
            }
        }
    }
    
    /**
     * RANK ADMIN DASHBOARD
     */
    @RestController
    @RequestMapping("/api/dashboard/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public class AdminDashboardController {
        /**
         * Get statistics for the current logged-in admin's dashboard
         * @param authentication The current authentication object
         * @return Map of statistics for the current admin
         */
        @GetMapping("/stats")
        public ResponseEntity<?> getMyAdminStats(Authentication authentication) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Long userId = userPrincipal.getId();
            
            Map<String, Object> stats = new HashMap<>();
            
            // Get stats for the current admin
            stats.put("managedRanksCount", rankAdminService.getRanksManagedCount(userId));
            List<TaxiRank> managedRanks = rankAdminService.getRanksManagedByAdmin(userId);
            stats.put("managedRanks", managedRanks);
            
            // Get terminal statistics for managed ranks
            long totalTerminals = 0;
            long activeTerminals = 0;
            Map<Long, Map<String, Long>> terminalStatsByRank = new HashMap<>();
            
            for (TaxiRank rank : managedRanks) {
                Long rankId = rank.getId();
                Long rankTerminals = terminalService.countTerminalsByRankId(rankId);
                Long rankActiveTerminals = terminalService.countActiveTerminalsByRankId(rankId);
                
                totalTerminals += rankTerminals;
                activeTerminals += rankActiveTerminals;
                
                Map<String, Long> rankStats = new HashMap<>();
                rankStats.put("total", rankTerminals);
                rankStats.put("active", rankActiveTerminals);
                terminalStatsByRank.put(rankId, rankStats);
            }
            
            stats.put("totalTerminals", totalTerminals);
            stats.put("activeTerminals", activeTerminals);
            stats.put("terminalStatsByRank", terminalStatsByRank);
            
            return ResponseEntity.ok(ApiResponse.success("Admin dashboard statistics retrieved", stats));
        }
    }
    
    /**
     * RIDER DASHBOARD
     */
    @RestController
    @RequestMapping("/api/dashboard/rider")
    @PreAuthorize("hasRole('RIDER')")
    public class RiderDashboardController {
        /**
         * Get statistics for the current logged-in rider's dashboard
         * @param authentication The current authentication object
         * @return Map of statistics for the current rider
         */
        @GetMapping("/stats")
        public ResponseEntity<?> getRiderStats(Authentication authentication) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Long userId = userPrincipal.getId();
            
            Map<String, Object> stats = new HashMap<>();
            
            // Basic statistics for riders
            stats.put("totalRanks", taxiRankService.getTotalRanksCount());
            
            // You could add more rider-specific statistics here
            // such as favorite ranks, recent trips, etc.
            
            return ResponseEntity.ok(ApiResponse.success("Rider dashboard statistics retrieved", stats));
        }
    }
    
    /**
     * PUBLIC DASHBOARD
     */
    @RestController
    @RequestMapping("/api/dashboard/public")
    public class PublicDashboardController {
        /**
         * Get public statistics about the system
         * @return Map of public statistics
         */
        @GetMapping("/stats")
        public ResponseEntity<?> getPublicStats() {
            Map<String, Object> stats = new HashMap<>();
            
            // Only include information that should be publicly available
            stats.put("totalRanks", taxiRankService.getTotalRanksCount());
            
            return ResponseEntity.ok(ApiResponse.success("Public dashboard statistics retrieved", stats));
        }
    }
    
    /**
     * BACKWARD COMPATIBILITY ENDPOINTS
     * These endpoints maintain backward compatibility with the old dashboard API
     */
    @RequestMapping("/api/dashboard")
    public class LegacyDashboardController {
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
            
            // Get terminal statistics across all ranks
            long totalTerminals = 0;
            long activeTerminals = 0;
            List<TaxiRank> allRanks = taxiRankService.getAllRanks();
            for (TaxiRank rank : allRanks) {
                totalTerminals += terminalService.countTerminalsByRankId(rank.getId());
                activeTerminals += terminalService.countActiveTerminalsByRankId(rank.getId());
            }
            stats.put("totalTerminals", totalTerminals);
            stats.put("activeTerminals", activeTerminals);
            
            return ResponseEntity.ok(ApiResponse.success("Dashboard statistics retrieved", stats));
        }
        
        /**
         * Get statistics for the current logged-in admin's dashboard
         * @param authentication The current authentication object
         * @return Map of statistics for the current admin
         */
        @GetMapping("/my-stats")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<?> getMyStats(Authentication authentication) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Long userId = userPrincipal.getId();
            
            Map<String, Object> stats = new HashMap<>();
            
            // Get stats for the current admin
            stats.put("managedRanksCount", rankAdminService.getRanksManagedCount(userId));
            List<TaxiRank> managedRanks = rankAdminService.getRanksManagedByAdmin(userId);
            stats.put("managedRanks", managedRanks);
            
            // Get terminal statistics for managed ranks
            long totalTerminals = 0;
            long activeTerminals = 0;
            Map<Long, Map<String, Long>> terminalStatsByRank = new HashMap<>();
            
            for (TaxiRank rank : managedRanks) {
                Long rankId = rank.getId();
                Long rankTerminals = terminalService.countTerminalsByRankId(rankId);
                Long rankActiveTerminals = terminalService.countActiveTerminalsByRankId(rankId);
                
                totalTerminals += rankTerminals;
                activeTerminals += rankActiveTerminals;
                
                Map<String, Long> rankStats = new HashMap<>();
                rankStats.put("total", rankTerminals);
                rankStats.put("active", rankActiveTerminals);
                terminalStatsByRank.put(rankId, rankStats);
            }
            
            stats.put("totalTerminals", totalTerminals);
            stats.put("activeTerminals", activeTerminals);
            stats.put("terminalStatsByRank", terminalStatsByRank);
            
            return ResponseEntity.ok(ApiResponse.success("Admin dashboard statistics retrieved", stats));
        }
        
        /**
         * Get all rank admins with their assigned ranks (for super admin)
         * @return List of rank admins with their assigned ranks
         */
        @GetMapping("/rank-admins")
        @PreAuthorize("hasRole('SUPER_ADMIN')")
        public ResponseEntity<?> getAllRankAdmins() {
            List<User> admins = userService.getUsersByRole(UserRole.ADMIN);
            List<RankAdminOverviewDTO> adminOverviews = new ArrayList<>();
            
            for (User admin : admins) {
                RankAdminOverviewDTO overview = new RankAdminOverviewDTO();
                overview.setId(admin.getId());
                overview.setFirstName(admin.getFirstName());
                overview.setLastName(admin.getLastName());
                overview.setEmail(admin.getEmail());
                overview.setPhoneNumber(admin.getPhoneNumber());
                
                // Get ranks managed by this admin
                List<TaxiRank> managedRanks = rankAdminService.getRanksManagedByAdmin(admin.getId());
                overview.setManagedRanks(managedRanks);
                overview.setRankCount(managedRanks.size());
                
                adminOverviews.add(overview);
            }
            
            return ResponseEntity.ok(ApiResponse.success("All rank admins retrieved", adminOverviews));
        }
        
        /**
         * Get the status of all taxi ranks (assigned/unassigned)
         * @return List of all taxi ranks with their admin assignment status
         */
        @GetMapping("/ranks-status")
        @PreAuthorize("hasRole('SUPER_ADMIN')")
        public ResponseEntity<?> getAllRanksStatus() {
            List<TaxiRank> allRanks = taxiRankService.getAllRanks();
            List<TaxiRankStatusDTO> rankStatusList = new ArrayList<>();
            
            for (TaxiRank rank : allRanks) {
                TaxiRankStatusDTO statusDTO = new TaxiRankStatusDTO();
                statusDTO.setId(rank.getId());
                statusDTO.setName(rank.getName());
                statusDTO.setCode(rank.getCode());
                statusDTO.setCity(rank.getCity());
                statusDTO.setProvince(rank.getProvince());
                
                // Check if rank has admin assigned
                List<User> assignedAdmins = rankAdminService.getAdminsForRank(rank.getId());
                statusDTO.setIsAssigned(!assignedAdmins.isEmpty());
                statusDTO.setAssignedAdmins(assignedAdmins);
                
                // Add terminal statistics
                statusDTO.setTerminalCount(terminalService.countTerminalsByRankId(rank.getId()));
                statusDTO.setActiveTerminalCount(terminalService.countActiveTerminalsByRankId(rank.getId()));
                
                rankStatusList.add(statusDTO);
            }
            
            return ResponseEntity.ok(ApiResponse.success("All ranks status retrieved", rankStatusList));
        }
        
        /**
         * Get details about a specific rank admin and their assigned ranks
         * @param adminId The ID of the admin
         * @return Details about the admin and their assigned ranks
         */
        @GetMapping("/rank-admins/{adminId}")
        @PreAuthorize("hasRole('SUPER_ADMIN')")
        public ResponseEntity<?> getRankAdminDetails(@PathVariable Long adminId) {
            try {
                User admin = userService.getUserById(adminId);
                
                if (admin.getRole() != UserRole.ADMIN) {
                    return ResponseEntity.badRequest()
                        .body(ApiResponse.error("User is not a rank admin"));
                }
                
                RankAdminOverviewDTO overview = new RankAdminOverviewDTO();
                overview.setId(admin.getId());
                overview.setFirstName(admin.getFirstName());
                overview.setLastName(admin.getLastName());
                overview.setEmail(admin.getEmail());
                overview.setPhoneNumber(admin.getPhoneNumber());
                
                // Get ranks managed by this admin
                List<TaxiRank> managedRanks = rankAdminService.getRanksManagedByAdmin(admin.getId());
                overview.setManagedRanks(managedRanks);
                overview.setRankCount(managedRanks.size());
                
                // Get the rank admin assignments to get permission details
                List<RankAdmin> assignments = rankAdminRepository.findByUser(admin);
                Map<Long, Map<String, Boolean>> rankPermissions = new HashMap<>();
                
                for (RankAdmin assignment : assignments) {
                    Map<String, Boolean> permissions = new HashMap<>();
                    permissions.put("canManageDrivers", assignment.isCanManageDrivers());
                    permissions.put("canViewFinancials", assignment.isCanViewFinancials());
                    permissions.put("canEditRankDetails", assignment.isCanEditRankDetails());
                    permissions.put("canManageRoutes", assignment.isCanManageRoutes());
                    permissions.put("canManageTerminals", assignment.isCanManageTerminals());
                    
                    rankPermissions.put(assignment.getTaxiRank().getId(), permissions);
                }
                
                overview.setRankPermissions(rankPermissions);
                
                return ResponseEntity.ok(ApiResponse.success("Rank admin details retrieved", overview));
            } catch (Exception e) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve rank admin details: " + e.getMessage()));
            }
        }
    }
    
    /**
     * Direct endpoint mapping for /my-stats for legacy/frontend compatibility
     */
    @GetMapping("/api/dashboard/my-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getMyStats(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();
        
        Map<String, Object> stats = new HashMap<>();
        
        // Get stats for the current admin
        stats.put("managedRanksCount", rankAdminService.getRanksManagedCount(userId));
        List<TaxiRank> managedRanks = rankAdminService.getRanksManagedByAdmin(userId);
        stats.put("managedRanks", managedRanks);
        
        // Get terminal statistics for managed ranks
        long totalTerminals = 0;
        long activeTerminals = 0;
        Map<Long, Map<String, Long>> terminalStatsByRank = new HashMap<>();
        
        for (TaxiRank rank : managedRanks) {
            Long rankId = rank.getId();
            Long rankTerminals = terminalService.countTerminalsByRankId(rankId);
            Long rankActiveTerminals = terminalService.countActiveTerminalsByRankId(rankId);
            
            totalTerminals += rankTerminals;
            activeTerminals += rankActiveTerminals;
            
            Map<String, Long> rankStats = new HashMap<>();
            rankStats.put("total", rankTerminals);
            rankStats.put("active", rankActiveTerminals);
            terminalStatsByRank.put(rankId, rankStats);
        }
        
        stats.put("totalTerminals", totalTerminals);
        stats.put("activeTerminals", activeTerminals);
        stats.put("terminalStatsByRank", terminalStatsByRank);
        
        return ResponseEntity.ok(ApiResponse.success("Admin dashboard statistics retrieved", stats));
    }
} 