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
    
    // Get all admins for a specific rank
    List<User> getAdminsForRank(Long rankId);
    
    // Check if a user is an admin for a specific rank
    boolean isUserAdminForRank(Long userId, Long rankId);
    
    // Remove an admin from a rank
    void removeAdminFromRank(Long userId, Long rankId);
    
    // Update admin permissions for a rank
    RankAdmin updateAdminPermissions(Long userId, Long rankId, RankAdminAssignmentDTO updateDTO);
    
    // Get specific admin-rank assignment
    RankAdmin getAdminRankAssignment(Long userId, Long rankId);
} 