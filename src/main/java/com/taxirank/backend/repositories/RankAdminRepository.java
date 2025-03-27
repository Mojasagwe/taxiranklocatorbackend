package com.taxirank.backend.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taxirank.backend.models.RankAdmin;
import com.taxirank.backend.models.TaxiRank;
import com.taxirank.backend.models.User;

@Repository
public interface RankAdminRepository extends JpaRepository<RankAdmin, Long> {
    // Find all admin assignments for a specific user
    List<RankAdmin> findByUser(User user);
    
    // Find all admin assignments for a specific rank
    List<RankAdmin> findByTaxiRank(TaxiRank taxiRank);
    
    // Find a specific user-rank admin assignment
    Optional<RankAdmin> findByUserAndTaxiRank(User user, TaxiRank taxiRank);
    
    // Check if a user is an admin for a specific rank
    boolean existsByUserAndTaxiRank(User user, TaxiRank taxiRank);
    
    // Get count of admins for a specific rank
    long countByTaxiRank(TaxiRank taxiRank);
} 