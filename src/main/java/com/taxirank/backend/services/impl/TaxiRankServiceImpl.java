package com.taxirank.backend.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taxirank.backend.models.TaxiRank;
import com.taxirank.backend.repositories.TaxiRankRepository;
import com.taxirank.backend.services.TaxiRankService;

@Service
public class TaxiRankServiceImpl implements TaxiRankService {

    @Autowired
    private TaxiRankRepository taxiRankRepository;
    
    @Override
    public List<TaxiRank> getAllRanks() {
        return taxiRankRepository.findAll();
    }

    @Override
    public TaxiRank getRankById(Long id) {
        return taxiRankRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Taxi rank not found"));
    }

    @Override
    public TaxiRank createRank(TaxiRank taxiRank) {
        return taxiRankRepository.save(taxiRank);
    }

    @Override
    public TaxiRank updateRank(Long id, TaxiRank taxiRankDetails) {
        TaxiRank taxiRank = taxiRankRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Taxi rank not found"));
        
        taxiRank.setName(taxiRankDetails.getName());
        taxiRank.setCode(taxiRankDetails.getCode());
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
}