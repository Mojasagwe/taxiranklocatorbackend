package com.taxirank.backend.repositories;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.taxirank.backend.models.TaxiRank;
import com.taxirank.backend.models.RankAdmin;

@Repository
public interface TaxiRankRepository extends JpaRepository<TaxiRank, Long> {
    Optional<TaxiRank> findByCode(String code);
    boolean existsByCode(String code);
    Optional<TaxiRank> findByName(String name);
    
    @Query("SELECT COUNT(t) FROM TaxiRank t WHERE NOT EXISTS (SELECT 1 FROM RankAdmin ra WHERE ra.taxiRank = t)")
    long countUnassignedRanks();
    
    @Query("SELECT COUNT(t) FROM TaxiRank t WHERE EXISTS (SELECT 1 FROM RankAdmin ra WHERE ra.taxiRank = t)")
    long countAssignedRanks();
    
    @Query("SELECT t FROM TaxiRank t WHERE NOT EXISTS (SELECT 1 FROM RankAdmin ra WHERE ra.taxiRank = t)")
    List<TaxiRank> findUnassignedRanks();
} 