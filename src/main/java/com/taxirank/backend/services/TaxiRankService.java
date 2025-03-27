package com.taxirank.backend.services;

import java.util.List;

import com.taxirank.backend.models.TaxiRank;

public interface TaxiRankService {
    // Get all taxi ranks
    List<TaxiRank> getAllRanks();
    
    // Get a specific taxi rank by ID
    TaxiRank getRankById(Long id);
    
    // Create a new taxi rank
    TaxiRank createRank(TaxiRank taxiRank);
    
    // Update a taxi rank
    TaxiRank updateRank(Long id, TaxiRank taxiRankDetails);
    
    // Delete a taxi rank
    void deleteRank(Long id);
}