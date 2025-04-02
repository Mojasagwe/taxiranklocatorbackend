package com.taxirank.backend.services;

import java.util.List;

import com.taxirank.backend.dto.RankAdminAssignmentDTO;
import com.taxirank.backend.models.RankAdmin;
import com.taxirank.backend.models.TaxiRank;
import com.taxirank.backend.models.User;

public interface RankAdminService {
    // Assign an admin to a rank
    RankAdmin assignAdminToRank(RankAdminAssignmentDTO assignmentDTO);
    
    // Get all ranks managed by a specific admin
    List<TaxiRank> getRanksManagedByAdmin(Long userId);
    
    // Count ranks managed by a specific admin
    long getRanksManagedCount(Long userId);
    
    // Get all admins for a specific rank
    List<User> getAdminsForRank(Long rankId);
    
    // Get all admins for a specific rank by code
    List<User> getAdminsForRankByCode(String rankCode);
    
    // Check if a user is an admin for a specific rank
    boolean isUserAdminForRank(Long userId, Long rankId);
    
    // Check if a user is an admin for a specific rank by code
    boolean isUserAdminForRankByCode(Long userId, String rankCode);
    
    // Remove an admin from a rank
    void removeAdminFromRank(Long userId, Long rankId);
    
    // Remove an admin from a rank by code
    void removeAdminFromRankByCode(Long userId, String rankCode);
    
    // Update admin permissions for a rank
    RankAdmin updateAdminPermissions(Long userId, Long rankId, RankAdminAssignmentDTO updateDTO);
    
    // Update admin permissions for a rank by code
    RankAdmin updateAdminPermissionsByCode(Long userId, String rankCode, RankAdminAssignmentDTO updateDTO);
    
    // Get specific admin-rank assignment
    RankAdmin getAdminRankAssignment(Long userId, Long rankId);
    
    // Get specific admin-rank assignment by code
    RankAdmin getAdminRankAssignmentByCode(Long userId, String rankCode);
} 