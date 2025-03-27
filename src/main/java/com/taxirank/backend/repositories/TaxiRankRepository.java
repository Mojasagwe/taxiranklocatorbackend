package com.taxirank.backend.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taxirank.backend.models.TaxiRank;

@Repository
public interface TaxiRankRepository extends JpaRepository<TaxiRank, Long> {
    Optional<TaxiRank> findByCode(String code);
    boolean existsByCode(String code);
    Optional<TaxiRank> findByName(String name);
} 