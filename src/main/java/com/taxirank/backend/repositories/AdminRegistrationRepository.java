package com.taxirank.backend.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taxirank.backend.enums.AdminRequestStatus;
import com.taxirank.backend.models.AdminRegistrationRequest;

@Repository
public interface AdminRegistrationRepository extends JpaRepository<AdminRegistrationRequest, Long> {
    // Find by email
    Optional<AdminRegistrationRequest> findByEmail(String email);
    
    // Find by status
    List<AdminRegistrationRequest> findByStatus(AdminRequestStatus status);
    
    // Find by email and status
    Optional<AdminRegistrationRequest> findByEmailAndStatus(String email, AdminRequestStatus status);
    
    // Check if exists by email
    boolean existsByEmail(String email);
}