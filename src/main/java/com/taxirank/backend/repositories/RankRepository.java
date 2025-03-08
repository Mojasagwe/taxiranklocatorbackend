package com.taxirank.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.taxirank.backend.models.Rank;

public interface RankRepository extends JpaRepository<Rank, Long> {
    // Add custom queries here if needed
}