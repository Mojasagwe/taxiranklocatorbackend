package com.taxirank.backend.services;

import java.util.List;

import com.taxirank.backend.models.TaxiRank;

public interface TaxiRankService {
    // Get all taxi ranks
    List<TaxiRank> getAllRanks();
    
    // Get all unassigned taxi ranks (ranks without an admin)
    List<TaxiRank> getUnassignedRanks();
    
    // Count total number of ranks
    long getTotalRanksCount();
    
    // Count total number of assigned ranks
    long getAssignedRanksCount();
    
    // Get a specific taxi rank by ID
    TaxiRank getRankById(Long id);
    
    // Get a specific taxi rank by code
    TaxiRank getRankByCode(String code);
    
    // Create a new taxi rank
    TaxiRank createRank(TaxiRank taxiRank);
    
    // Update a taxi rank
    TaxiRank updateRank(Long id, TaxiRank taxiRankDetails);
    
    // Delete a taxi rank
    void deleteRank(Long id);
    
    // Generate a unique rank code
    String generateRankCode(String rankName, String province);
    
    // Check if a rank code exists
    boolean rankCodeExists(String code);
}