package com.taxirank.backend.services.impl;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public TaxiRank createRank(TaxiRank taxiRank) {
        // Generate code if not provided
        if (taxiRank.getCode() == null || taxiRank.getCode().isEmpty()) {
            taxiRank.setCode(generateRankCode(taxiRank.getName(), taxiRank.getProvince()));
        }
        
        // Validate unique code
        if (rankCodeExists(taxiRank.getCode())) {
            throw new RuntimeException("Rank code already exists: " + taxiRank.getCode());
        }
        
        return taxiRankRepository.save(taxiRank);
    }

    @Override
    @Transactional
    public TaxiRank updateRank(Long id, TaxiRank taxiRankDetails) {
        TaxiRank taxiRank = taxiRankRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Taxi rank not found"));
        
        // If code is changed, check if the new code already exists
        if (taxiRankDetails.getCode() != null && !taxiRankDetails.getCode().isEmpty() 
                && !taxiRankDetails.getCode().equals(taxiRank.getCode())) {
            if (rankCodeExists(taxiRankDetails.getCode())) {
                throw new RuntimeException("Rank code already exists: " + taxiRankDetails.getCode());
            }
            taxiRank.setCode(taxiRankDetails.getCode());
        }
        
        taxiRank.setName(taxiRankDetails.getName());
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
    @Transactional
    public void deleteRank(Long id) {
        TaxiRank taxiRank = taxiRankRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Taxi rank not found"));
        
        // Check if rank has admins assigned
        if (rankAdminRepository.countByTaxiRankId(id) > 0) {
            throw new RuntimeException("Cannot delete rank with assigned admins");
        }
        
        taxiRankRepository.delete(taxiRank);
    }
    
    @Override
    public String generateRankCode(String rankName, String province) {
        // Extract first 3 characters of the rank name (or less if shorter)
        String namePrefix = rankName.replaceAll("[^a-zA-Z0-9]", "").substring(0, Math.min(rankName.length(), 3));
        
        // Extract first character of the province
        String provincePrefix = "";
        if (province != null && !province.isEmpty()) {
            provincePrefix = province.substring(0, 1);
        }
        
        // Generate a 3-digit sequential number
        String numberSuffix = String.format("%03d", new Random().nextInt(1000));
        
        // Combine to form the code
        String code = (namePrefix + provincePrefix + numberSuffix).toUpperCase();
        
        // Ensure uniqueness
        int attempt = 0;
        String finalCode = code;
        while (rankCodeExists(finalCode) && attempt < 10) {
            numberSuffix = String.format("%03d", new Random().nextInt(1000));
            finalCode = (namePrefix + provincePrefix + numberSuffix).toUpperCase();
            attempt++;
        }
        
        return finalCode;
    }
    
    @Override
    public boolean rankCodeExists(String code) {
        return taxiRankRepository.existsByCode(code);
    }
}