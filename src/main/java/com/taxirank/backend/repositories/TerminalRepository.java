package com.taxirank.backend.repositories;

import com.taxirank.backend.models.Terminal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TerminalRepository extends JpaRepository<Terminal, Long> {
    
    /**
     * Find all terminals belonging to a specific taxi rank
     */
    List<Terminal> findByTaxiRankId(Long taxiRankId);
    
    /**
     * Find all active terminals belonging to a specific taxi rank
     */
    List<Terminal> findByTaxiRankIdAndIsActiveTrue(Long taxiRankId);
    
    /**
     * Find all terminals belonging to a taxi rank with the given code
     */
    List<Terminal> findByTaxiRankCode(String rankCode);
    
    /**
     * Find all active terminals belonging to a taxi rank with the given code
     */
    List<Terminal> findByTaxiRankCodeAndIsActiveTrue(String rankCode);
    
    /**
     * Find a specific terminal belonging to a specific taxi rank
     */
    Optional<Terminal> findByIdAndTaxiRankId(Long id, Long taxiRankId);
    
    /**
     * Count the number of terminals for a specific taxi rank
     */
    Long countByTaxiRankId(Long taxiRankId);
    
    /**
     * Count the number of active terminals for a specific taxi rank
     */
    Long countByTaxiRankIdAndIsActiveTrue(Long taxiRankId);
} 