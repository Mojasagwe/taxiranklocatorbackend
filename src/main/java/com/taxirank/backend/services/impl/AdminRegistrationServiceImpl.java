package com.taxirank.backend.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taxirank.backend.dto.AdminRegistrationRequest;
import com.taxirank.backend.dto.AdminReviewRequest;
import com.taxirank.backend.dto.RankAdminAssignmentDTO;
import com.taxirank.backend.enums.AdminRequestStatus;
import com.taxirank.backend.enums.UserRole;
import com.taxirank.backend.models.TaxiRank;
import com.taxirank.backend.models.User;
import com.taxirank.backend.repositories.AdminRegistrationRepository;
import com.taxirank.backend.repositories.TaxiRankRepository;
import com.taxirank.backend.repositories.UserRepository;
import com.taxirank.backend.repositories.RankAdminRepository;
import com.taxirank.backend.services.AdminRegistrationService;
import com.taxirank.backend.services.RankAdminService;

@Service
public class AdminRegistrationServiceImpl implements AdminRegistrationService {

    @Autowired
    private AdminRegistrationRepository adminRegistrationRepository;
    
    @Autowired
    private TaxiRankRepository taxiRankRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RankAdminService rankAdminService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private RankAdminRepository rankAdminRepository;
    
    @Override
    @Transactional
    public com.taxirank.backend.models.AdminRegistrationRequest submitRequest(AdminRegistrationRequest requestDTO) {
        // Check if email already exists in users
        if (userRepository.findByEmail(requestDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        
        // Check if email already exists in pending requests
        if (adminRegistrationRepository.existsByEmail(requestDTO.getEmail())) {
            throw new RuntimeException("A registration request with this email is already pending");
        }
        
        // Debug
        System.out.println("Request DTO: " + requestDTO);
        System.out.println("Selected Rank Codes: " + requestDTO.getSelectedRankCodes());
        System.out.println("Legacy - Selected Rank IDs: " + requestDTO.getSelectedRankIds());
        System.out.println("Legacy - Rank IDs: " + requestDTO.getRankIds());
        
        // Create new admin request
        com.taxirank.backend.models.AdminRegistrationRequest request = new com.taxirank.backend.models.AdminRegistrationRequest();
        request.setFirstName(requestDTO.getFirstName());
        request.setLastName(requestDTO.getLastName());
        request.setEmail(requestDTO.getEmail());
        request.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        request.setPhoneNumber(requestDTO.getPhoneNumber());
        request.setPreferredPaymentMethod(requestDTO.getPreferredPaymentMethod());
        request.setDesignation(requestDTO.getDesignation());
        request.setJustification(requestDTO.getJustification());
        request.setProfessionalExperience(requestDTO.getProfessionalExperience());
        request.setAdminNotes(requestDTO.getAdminNotes());
        
        // First try to add ranks by code (preferred new method)
        List<String> effectiveRankCodes = requestDTO.getEffectiveRankCodes();
        if (effectiveRankCodes != null && !effectiveRankCodes.isEmpty()) {
            System.out.println("Finding taxi ranks with codes: " + effectiveRankCodes);
            List<TaxiRank> ranks = new ArrayList<>();
            
            for (String code : effectiveRankCodes) {
                try {
                    TaxiRank rank = taxiRankRepository.findByCode(code)
                            .orElseThrow(() -> new RuntimeException("Taxi rank not found with code: " + code));
                    ranks.add(rank);
                } catch (Exception e) {
                    System.out.println("Error finding rank with code " + code + ": " + e.getMessage());
                }
            }
            
            System.out.println("Found ranks by code: " + ranks);
            if (!ranks.isEmpty()) {
                request.getSelectedRanks().addAll(ranks);
                System.out.println("Added ranks by code to request: " + request.getSelectedRanks());
            }
        } 
        // Fallback to IDs if no ranks were found by code (backward compatibility)
        else if (request.getSelectedRanks().isEmpty()) {
            List<Long> effectiveRankIds = requestDTO.getEffectiveRankIds();
            if (effectiveRankIds != null && !effectiveRankIds.isEmpty()) {
                System.out.println("Finding taxi ranks with IDs: " + effectiveRankIds);
                List<TaxiRank> ranks = taxiRankRepository.findAllById(effectiveRankIds);
                System.out.println("Found ranks: " + ranks);
                if (ranks != null && !ranks.isEmpty()) {
                    request.getSelectedRanks().addAll(ranks);
                    System.out.println("Added ranks to request: " + request.getSelectedRanks());
                }
            }
        }
        
        com.taxirank.backend.models.AdminRegistrationRequest savedRequest = adminRegistrationRepository.save(request);
        System.out.println("Saved request with ranks: " + savedRequest.getSelectedRanks());
        return savedRequest;
    }

    @Override
    public List<com.taxirank.backend.models.AdminRegistrationRequest> getPendingRequests() {
        return adminRegistrationRepository.findByStatus(AdminRequestStatus.PENDING);
    }

    @Override
    public List<com.taxirank.backend.models.AdminRegistrationRequest> getRequestsByStatus(AdminRequestStatus status) {
        return adminRegistrationRepository.findByStatus(status);
    }

    @Override
    public com.taxirank.backend.models.AdminRegistrationRequest getRequestById(Long id) {
        return adminRegistrationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin registration request not found"));
    }

    @Override
    @Transactional
    public com.taxirank.backend.models.AdminRegistrationRequest reviewRequest(Long requestId, AdminReviewRequest reviewDTO, Long reviewerId) {
        com.taxirank.backend.models.AdminRegistrationRequest request = adminRegistrationRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Admin registration request not found"));
        
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));
        
        // Check request status is PENDING
        if (request.getStatus() != AdminRequestStatus.PENDING) {
            throw new RuntimeException("Request has already been processed");
        }
        
        // Update request status and review info
        request.setStatus(reviewDTO.getStatus());
        request.setReviewNotes(reviewDTO.getReviewNotes());
        request.setReviewedBy(reviewer);
        request.setReviewedAt(LocalDateTime.now());
        
        // If approved, create the admin user
        if (reviewDTO.getStatus() == AdminRequestStatus.APPROVED) {
            // First, check if any of the requested ranks already have admins
            for (TaxiRank rank : request.getSelectedRanks()) {
                if (rankAdminRepository.countByTaxiRank(rank) > 0) {
                    // Found a rank with an existing admin - reject the request
                    request.setStatus(AdminRequestStatus.REJECTED);
                    request.setReviewNotes(reviewDTO.getReviewNotes() + " (Automatically rejected: Rank '" + 
                            rank.getName() + "' already has an admin assigned to it)");
                    return adminRegistrationRepository.save(request);
                }
            }
            
            // No existing admins found for the requested ranks, proceed with approval
            User newAdmin = new User();
            newAdmin.setFirstName(request.getFirstName());
            newAdmin.setLastName(request.getLastName());
            newAdmin.setEmail(request.getEmail());
            newAdmin.setPassword(request.getPassword()); // Already encoded during submission
            newAdmin.setPhoneNumber(request.getPhoneNumber());
            newAdmin.setPreferredPaymentMethod(request.getPreferredPaymentMethod());
            newAdmin.setRole(UserRole.ADMIN);
            
            User savedAdmin = userRepository.save(newAdmin);
            
            // Assign the admin to the requested ranks
            for (TaxiRank rank : request.getSelectedRanks()) {
                RankAdminAssignmentDTO assignmentDTO = new RankAdminAssignmentDTO();
                assignmentDTO.setUserId(savedAdmin.getId());
                assignmentDTO.setRankCode(rank.getCode());
                assignmentDTO.setDesignation(request.getDesignation());
                assignmentDTO.setNotes(request.getAdminNotes());
                assignmentDTO.setCanManageDrivers(true);
                assignmentDTO.setCanViewFinancials(true);
                assignmentDTO.setCanEditRankDetails(true);
                assignmentDTO.setCanManageRoutes(true);
                
                rankAdminService.assignAdminToRank(assignmentDTO);
            }
        }
        
        return adminRegistrationRepository.save(request);
    }

    @Override
    public boolean isPendingRequestExistsForEmail(String email) {
        return adminRegistrationRepository.findByEmailAndStatus(email, AdminRequestStatus.PENDING).isPresent();
    }

    @Override
    public boolean isRankAlreadyAssigned(Long rankId) {
        TaxiRank taxiRank = taxiRankRepository.findById(rankId)
                .orElseThrow(() -> new RuntimeException("Taxi rank not found"));
        
        return rankAdminRepository.countByTaxiRank(taxiRank) > 0;
    }

    @Override
    public boolean isRankAlreadyAssignedByCode(String rankCode) {
        TaxiRank taxiRank = taxiRankRepository.findByCode(rankCode)
                .orElseThrow(() -> new RuntimeException("Taxi rank not found with code: " + rankCode));
        
        return rankAdminRepository.countByTaxiRank(taxiRank) > 0;
    }
}