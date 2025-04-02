package com.taxirank.backend.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taxirank.backend.models.TaxiRank;
import com.taxirank.backend.repositories.RankAdminRepository;
import com.taxirank.backend.repositories.TaxiRankRepository;
import com.taxirank.backend.services.TaxiRankService;

@Service
public class TaxiRankServiceImpl implements TaxiRankService {

    @Autowired
    private TaxiRankRepository taxiRankRepository;
    
    @Autowired
    private RankAdminRepository rankAdminRepository;
    
    @Override
    public List<TaxiRank> getAllRanks() {
        return taxiRankRepository.findAll();
    }
    
    @Override
    public List<TaxiRank> getUnassignedRanks() {
        return taxiRankRepository.findUnassignedRanks();
    }

    @Override
    public long getTotalRanksCount() {
        return taxiRankRepository.count();
    }
    
    @Override
    public long getAssignedRanksCount() {
        return taxiRankRepository.countAssignedRanks();
    }

    @Override
    public TaxiRank getRankById(Long id) {
        return taxiRankRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Taxi rank not found"));
    }
    
    @Override
    public TaxiRank getRankByCode(String code) {
        return taxiRankRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Taxi rank not found with code: " + code));
    }

    @Override
    public TaxiRank createRank(TaxiRank taxiRank) {
        // Generate code if not provided
        if (taxiRank.getCode() == null || taxiRank.getCode().isEmpty()) {
            taxiRank.setCode(generateRankCode(taxiRank.getName(), taxiRank.getProvince()));
        }
        return taxiRankRepository.save(taxiRank);
    }

    @Override
    public TaxiRank updateRank(Long id, TaxiRank taxiRankDetails) {
        TaxiRank taxiRank = taxiRankRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Taxi rank not found"));
        
        taxiRank.setName(taxiRankDetails.getName());
        // Only update code if it's explicitly provided
        if (taxiRankDetails.getCode() != null && !taxiRankDetails.getCode().isEmpty()) {
            taxiRank.setCode(taxiRankDetails.getCode());
        }
        taxiRank.setDescription(taxiRankDetails.getDescription());
        taxiRank.setAddress(taxiRankDetails.getAddress());
        taxiRank.setCity(taxiRankDetails.getCity());
        taxiRank.setProvince(taxiRankDetails.getProvince());
        taxiRank.setLatitude(taxiRankDetails.getLatitude());
        taxiRank.setLongitude(taxiRankDetails.getLongitude());
        taxiRank.setContactPhone(taxiRankDetails.getContactPhone());
        taxiRank.setContactEmail(taxiRankDetails.getContactEmail());
        taxiRank.setOperatingHours(taxiRankDetails.getOperatingHours());
        taxiRank.setCapacity(taxiRankDetails.getCapacity());
        taxiRank.setIsActive(taxiRankDetails.getIsActive());
        
        return taxiRankRepository.save(taxiRank);
    }

    @Override
    public void deleteRank(Long id) {
        TaxiRank taxiRank = taxiRankRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Taxi rank not found"));
        
        taxiRankRepository.delete(taxiRank);
    }
    
    @Override
    public String generateRankCode(String rankName, String province) {
        if (rankName == null || rankName.isEmpty()) {
            throw new IllegalArgumentException("Rank name cannot be empty");
        }
        
        if (province == null || province.isEmpty()) {
            throw new IllegalArgumentException("Province cannot be empty");
        }
        
        // Extract first 3 letters of rank name and convert to uppercase
        String namePrefix = rankName.length() >= 3 
                ? rankName.substring(0, 3).toUpperCase() 
                : rankName.toUpperCase();
        
        // Extract first 2 letters of province and convert to uppercase
        String provincePrefix = province.length() >= 2 
                ? province.substring(0, 2).toUpperCase() 
                : province.toUpperCase();
        
        // Base code format
        String baseCode = namePrefix + provincePrefix;
        
        // Find next available increment
        int increment = 0;
        String proposedCode;
        
        do {
            proposedCode = baseCode + String.format("%02d", increment);
            increment++;
        } while (rankCodeExists(proposedCode));
        
        return proposedCode;
    }
    
    @Override
    public boolean rankCodeExists(String code) {
        return taxiRankRepository.existsByCode(code);
    }
}