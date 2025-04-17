package com.taxirank.backend.services.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taxirank.backend.dto.RankAssignmentRequestDTO;
import com.taxirank.backend.dto.RankAssignmentResponseDTO;
import com.taxirank.backend.enums.AdminRequestStatus;
import com.taxirank.backend.models.RankAdmin;
import com.taxirank.backend.models.RankAssignmentRequest;
import com.taxirank.backend.models.TaxiRank;
import com.taxirank.backend.models.User;
import com.taxirank.backend.repositories.RankAdminRepository;
import com.taxirank.backend.repositories.RankAssignmentRequestRepository;
import com.taxirank.backend.repositories.TaxiRankRepository;
import com.taxirank.backend.repositories.UserRepository;
import com.taxirank.backend.services.RankAssignmentRequestService;

@Service
@Transactional
public class RankAssignmentRequestServiceImpl implements RankAssignmentRequestService {

    @Autowired
    private RankAssignmentRequestRepository rankAssignmentRequestRepository;
    
    @Autowired
    private RankAdminRepository rankAdminRepository;
    
    @Autowired
    private TaxiRankRepository taxiRankRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public RankAssignmentRequest submitRequest(RankAssignmentRequestDTO requestDTO, Long adminId) {
        // Get the user
        User user = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get the rank admin
        RankAdmin requestingAdmin = rankAdminRepository.findByUser(user)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        
        // Get the taxi rank based on either rankId or rankCode
        TaxiRank requestedRank;
        if (requestDTO.getRankId() != null) {
            requestedRank = taxiRankRepository.findById(requestDTO.getRankId())
                    .orElseThrow(() -> new RuntimeException("Taxi rank not found"));
        } else if (requestDTO.getRankCode() != null && !requestDTO.getRankCode().isEmpty()) {
            requestedRank = taxiRankRepository.findByCode(requestDTO.getRankCode())
                    .orElseThrow(() -> new RuntimeException("Taxi rank not found with code: " + requestDTO.getRankCode()));
        } else {
            throw new RuntimeException("Either rankId or rankCode must be provided");
        }
        
        // Check if the admin is already assigned to this rank
        if (rankAdminRepository.existsByUserAndTaxiRank(user, requestedRank)) {
            throw new RuntimeException("You are already an admin for this rank");
        }
        
        // Check if there's a pending request for this admin and rank
        if (rankAssignmentRequestRepository.existsByRequestingAdminAndRequestedRankAndStatus(
                requestingAdmin, requestedRank, AdminRequestStatus.PENDING)) {
            throw new RuntimeException("You already have a pending request for this rank");
        }
        
        // Create a new request
        RankAssignmentRequest request = new RankAssignmentRequest();
        request.setRequestingAdmin(requestingAdmin);
        request.setRequestedRank(requestedRank);
        request.setStatus(AdminRequestStatus.PENDING);
        request.setRequestReason(requestDTO.getRequestReason());
        request.setRequestDate(LocalDateTime.now());
        
        return rankAssignmentRequestRepository.save(request);
    }

    @Override
    public List<RankAssignmentRequest> getRequestsByStatus(AdminRequestStatus status) {
        if (status == null) {
            return rankAssignmentRequestRepository.findAll();
        }
        return rankAssignmentRequestRepository.findByStatus(status);
    }

    @Override
    public List<RankAssignmentRequest> getRequestsByAdmin(Long adminId) {
        // Get the user
        User user = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get the rank admin
        RankAdmin admin = rankAdminRepository.findByUser(user)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        
        return rankAssignmentRequestRepository.findByRequestingAdmin(admin);
    }

    @Override
    public List<RankAssignmentRequest> getRequestsByRank(Long rankId) {
        TaxiRank rank = taxiRankRepository.findById(rankId)
                .orElseThrow(() -> new RuntimeException("Taxi rank not found"));
        
        return rankAssignmentRequestRepository.findByRequestedRank(rank);
    }
    
    @Override
    public List<RankAssignmentRequest> getRequestsByRankCode(String rankCode) {
        TaxiRank rank = taxiRankRepository.findByCode(rankCode)
                .orElseThrow(() -> new RuntimeException("Taxi rank not found with code: " + rankCode));
        
        return rankAssignmentRequestRepository.findByRequestedRank(rank);
    }

    @Override
    public RankAssignmentRequest getRequestById(Long requestId) {
        return rankAssignmentRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Rank assignment request not found"));
    }

    @Override
    public RankAssignmentRequest reviewRequest(Long requestId, RankAssignmentResponseDTO responseDTO, Long reviewerId) {
        // Get the request
        RankAssignmentRequest request = rankAssignmentRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Rank assignment request not found"));
        
        // Check if the request is in a reviewable state
        if (request.getStatus() != AdminRequestStatus.PENDING) {
            throw new RuntimeException("This request cannot be reviewed as it is " + request.getStatus());
        }
        
        // Get the reviewer
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));
        
        // Update the request
        request.setStatus(responseDTO.getStatus());
        request.setResponseMessage(responseDTO.getResponseMessage());
        request.setReviewedBy(reviewer);
        request.setResponseDate(LocalDateTime.now());
        
        // If approved, assign the admin to the rank
        if (responseDTO.getStatus() == AdminRequestStatus.APPROVED) {
            // Check if another admin is already assigned to this rank
            if (rankAdminRepository.countByTaxiRank(request.getRequestedRank()) > 0) {
                request.setStatus(AdminRequestStatus.REJECTED);
                request.setResponseMessage(
                    "This rank already has an admin assigned to it. Request automatically rejected."
                );
                return rankAssignmentRequestRepository.save(request);
            }
            
            // Create a new rank-admin assignment
            RankAdmin rankAdmin = new RankAdmin();
            rankAdmin.setUser(request.getRequestingAdmin().getUser());
            rankAdmin.setTaxiRank(request.getRequestedRank());
            
            // Copy permissions from existing admin assignment
            rankAdmin.setCanManageDrivers(request.getRequestingAdmin().isCanManageDrivers());
            rankAdmin.setCanViewFinancials(request.getRequestingAdmin().isCanViewFinancials());
            rankAdmin.setCanEditRankDetails(request.getRequestingAdmin().isCanEditRankDetails());
            rankAdmin.setCanManageRoutes(request.getRequestingAdmin().isCanManageRoutes());
            rankAdmin.setCanManageTerminals(request.getRequestingAdmin().isCanManageTerminals());
            
            rankAdminRepository.save(rankAdmin);
        }
        
        return rankAssignmentRequestRepository.save(request);
    }

    @Override
    public RankAssignmentRequest cancelRequest(Long requestId, Long adminId) {
        // Get the request
        RankAssignmentRequest request = rankAssignmentRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Rank assignment request not found"));
        
        // Get the user
        User user = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get the rank admin
        RankAdmin admin = rankAdminRepository.findByUser(user)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        
        if (!request.getRequestingAdmin().equals(admin)) {
            throw new RuntimeException("You are not authorized to cancel this request");
        }
        
        // Check if the request is in a cancellable state
        if (request.getStatus() != AdminRequestStatus.PENDING) {
            throw new RuntimeException("This request cannot be cancelled as it is " + request.getStatus());
        }
        
        // Update the request
        request.setStatus(AdminRequestStatus.CANCELLED);
        request.setResponseDate(LocalDateTime.now());
        
        return rankAssignmentRequestRepository.save(request);
    }

    @Override
    public boolean hasPendingRequestsForRank(Long rankId) {
        TaxiRank rank = taxiRankRepository.findById(rankId)
                .orElseThrow(() -> new RuntimeException("Taxi rank not found"));
        
        return !rankAssignmentRequestRepository.findByRequestedRankAndStatus(rank, AdminRequestStatus.PENDING).isEmpty();
    }

    @Override
    public boolean hasAdminPendingRequestForRank(Long adminId, Long rankId) {
        // Get the user
        User user = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get the rank admin
        RankAdmin admin = rankAdminRepository.findByUser(user)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        
        TaxiRank rank = taxiRankRepository.findById(rankId)
                .orElseThrow(() -> new RuntimeException("Taxi rank not found"));
        
        return rankAssignmentRequestRepository.existsByRequestingAdminAndRequestedRankAndStatus(
                admin, rank, AdminRequestStatus.PENDING);
    }
    
    @Override
    public boolean hasAdminPendingRequestForRankByCode(Long adminId, String rankCode) {
        // Get the user
        User user = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get the rank admin
        RankAdmin admin = rankAdminRepository.findByUser(user)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        
        TaxiRank rank = taxiRankRepository.findByCode(rankCode)
                .orElseThrow(() -> new RuntimeException("Taxi rank not found with code: " + rankCode));
        
        return rankAssignmentRequestRepository.existsByRequestingAdminAndRequestedRankAndStatus(
                admin, rank, AdminRequestStatus.PENDING);
    }
} 