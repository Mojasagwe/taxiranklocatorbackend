package com.taxirank.backend.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taxirank.backend.dto.RankAdminAssignmentDTO;
import com.taxirank.backend.enums.UserRole;
import com.taxirank.backend.models.RankAdmin;
import com.taxirank.backend.models.TaxiRank;
import com.taxirank.backend.models.User;
import com.taxirank.backend.repositories.RankAdminRepository;
import com.taxirank.backend.repositories.TaxiRankRepository;
import com.taxirank.backend.repositories.UserRepository;
import com.taxirank.backend.services.RankAdminService;

@Service
@Transactional
public class RankAdminServiceImpl implements RankAdminService {

    @Autowired
    private RankAdminRepository rankAdminRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TaxiRankRepository taxiRankRepository;
    
    @Override
    public RankAdmin assignAdminToRank(RankAdminAssignmentDTO assignmentDTO) {
        User user = userRepository.findById(assignmentDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        TaxiRank taxiRank = taxiRankRepository.findById(assignmentDTO.getRankId())
                .orElseThrow(() -> new RuntimeException("Taxi rank not found"));
        
        // Ensure user has ADMIN role
        if (user.getRole() != UserRole.ADMIN) {
            user.setRole(UserRole.ADMIN);
            userRepository.save(user);
        }
        
        // Check if assignment already exists
        if (rankAdminRepository.existsByUserAndTaxiRank(user, taxiRank)) {
            throw new RuntimeException("User is already an admin for this rank");
        }
        
        // Create new assignment
        RankAdmin rankAdmin = new RankAdmin();
        rankAdmin.setUser(user);
        rankAdmin.setTaxiRank(taxiRank);
        rankAdmin.setDesignation(assignmentDTO.getDesignation());
        rankAdmin.setNotes(assignmentDTO.getNotes());
        
        // Set permissions
        if (assignmentDTO.getCanManageDrivers() != null) {
            rankAdmin.setCanManageDrivers(assignmentDTO.getCanManageDrivers());
        }
        if (assignmentDTO.getCanViewFinancials() != null) {
            rankAdmin.setCanViewFinancials(assignmentDTO.getCanViewFinancials());
        }
        if (assignmentDTO.getCanEditRankDetails() != null) {
            rankAdmin.setCanEditRankDetails(assignmentDTO.getCanEditRankDetails());
        }
        if (assignmentDTO.getCanManageRoutes() != null) {
            rankAdmin.setCanManageRoutes(assignmentDTO.getCanManageRoutes());
        }
        
        return rankAdminRepository.save(rankAdmin);
    }

    @Override
    public List<TaxiRank> getRanksManagedByAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return rankAdminRepository.findByUser(user).stream()
                .map(RankAdmin::getTaxiRank)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getAdminsForRank(Long rankId) {
        TaxiRank taxiRank = taxiRankRepository.findById(rankId)
                .orElseThrow(() -> new RuntimeException("Taxi rank not found"));
        
        return rankAdminRepository.findByTaxiRank(taxiRank).stream()
                .map(RankAdmin::getUser)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isUserAdminForRank(Long userId, Long rankId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        TaxiRank taxiRank = taxiRankRepository.findById(rankId)
                .orElseThrow(() -> new RuntimeException("Taxi rank not found"));
        
        return rankAdminRepository.existsByUserAndTaxiRank(user, taxiRank);
    }

    @Override
    public void removeAdminFromRank(Long userId, Long rankId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        TaxiRank taxiRank = taxiRankRepository.findById(rankId)
                .orElseThrow(() -> new RuntimeException("Taxi rank not found"));
        
        RankAdmin rankAdmin = rankAdminRepository.findByUserAndTaxiRank(user, taxiRank)
                .orElseThrow(() -> new RuntimeException("User is not an admin for this rank"));
        
        rankAdminRepository.delete(rankAdmin);
        
        // Check if user still has any rank admin roles
        if (rankAdminRepository.findByUser(user).isEmpty()) {
            // If not, consider demoting from ADMIN role (optional)
            // user.setRole(UserRole.RIDER);
            // userRepository.save(user);
        }
    }

    @Override
    public RankAdmin updateAdminPermissions(Long userId, Long rankId, RankAdminAssignmentDTO updateDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        TaxiRank taxiRank = taxiRankRepository.findById(rankId)
                .orElseThrow(() -> new RuntimeException("Taxi rank not found"));
        
        RankAdmin rankAdmin = rankAdminRepository.findByUserAndTaxiRank(user, taxiRank)
                .orElseThrow(() -> new RuntimeException("User is not an admin for this rank"));
        
        // Update fields
        if (updateDTO.getDesignation() != null) {
            rankAdmin.setDesignation(updateDTO.getDesignation());
        }
        if (updateDTO.getNotes() != null) {
            rankAdmin.setNotes(updateDTO.getNotes());
        }
        
        // Update permissions
        if (updateDTO.getCanManageDrivers() != null) {
            rankAdmin.setCanManageDrivers(updateDTO.getCanManageDrivers());
        }
        if (updateDTO.getCanViewFinancials() != null) {
            rankAdmin.setCanViewFinancials(updateDTO.getCanViewFinancials());
        }
        if (updateDTO.getCanEditRankDetails() != null) {
            rankAdmin.setCanEditRankDetails(updateDTO.getCanEditRankDetails());
        }
        if (updateDTO.getCanManageRoutes() != null) {
            rankAdmin.setCanManageRoutes(updateDTO.getCanManageRoutes());
        }
        
        return rankAdminRepository.save(rankAdmin);
    }

    @Override
    public RankAdmin getAdminRankAssignment(Long userId, Long rankId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        TaxiRank taxiRank = taxiRankRepository.findById(rankId)
                .orElseThrow(() -> new RuntimeException("Taxi rank not found"));
        
        return rankAdminRepository.findByUserAndTaxiRank(user, taxiRank)
                .orElseThrow(() -> new RuntimeException("User is not an admin for this rank"));
    }
} 