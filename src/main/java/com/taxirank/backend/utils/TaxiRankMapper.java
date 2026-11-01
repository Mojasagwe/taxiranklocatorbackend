package com.taxirank.backend.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.taxirank.backend.dto.TaxiRankDTO;
import com.taxirank.backend.models.TaxiRank;
import com.taxirank.backend.repositories.UserRepository;
import com.taxirank.backend.services.TerminalService;

/**
 * Mapper for converting between TaxiRank entities and DTOs
 */
@Component
public class TaxiRankMapper {

    private UserRepository userRepository;
    private TerminalService terminalService;

    @Autowired
    public TaxiRankMapper(UserRepository userRepository, TerminalService terminalService) {
        this.userRepository = userRepository;
        this.terminalService = terminalService;
    }

    /**
     * Convert TaxiRank entity to DTO
     *
     * @param taxiRank TaxiRank entity to convert
     * @param includeTerminalStats Whether to include terminal statistics
     * @return Converted DTO
     */
    public TaxiRankDTO toDTO(TaxiRank taxiRank, boolean includeTerminalStats) {
        if (taxiRank == null) {
            return null;
        }
        
        TaxiRankDTO dto = new TaxiRankDTO();
        
        // Map basic fields
        dto.setId(taxiRank.getId());
        dto.setName(taxiRank.getName());
        dto.setCode(taxiRank.getCode());
        dto.setDescription(taxiRank.getDescription());
        dto.setAddress(taxiRank.getAddress());
        dto.setCity(taxiRank.getCity());
        dto.setProvince(taxiRank.getProvince());
        dto.setLatitude(taxiRank.getLatitude());
        dto.setLongitude(taxiRank.getLongitude());
        dto.setContactPhone(taxiRank.getContactPhone());
        dto.setContactEmail(taxiRank.getContactEmail());
        dto.setOperatingHours(taxiRank.getOperatingHours());
        dto.setCapacity(taxiRank.getCapacity());
        dto.setIsActive(taxiRank.getIsActive());
        dto.setCreatedAt(taxiRank.getCreatedAt());
        dto.setUpdatedAt(taxiRank.getUpdatedAt());
        
        if (taxiRank.getId() != null) {
            dto.setHasAccount(userRepository.existsByRankId(taxiRank.getId()));
        }

        // Include terminal statistics if requested
        if (includeTerminalStats) {
            Long rankId = taxiRank.getId();
            dto.setTerminalCount(terminalService.countTerminalsByRankId(rankId));
            dto.setActiveTerminalCount(terminalService.countActiveTerminalsByRankId(rankId));
        }
        
        return dto;
    }
    
    /**
     * Shorthand method to convert entity to DTO with minimal information
     */
    public TaxiRankDTO toDTO(TaxiRank taxiRank) {
        return toDTO(taxiRank, false);
    }

    /**
     * Convert list of TaxiRank entities to list of DTOs
     */
    public List<TaxiRankDTO> toDTOList(List<TaxiRank> taxiRanks, boolean includeTerminalStats) {
        return taxiRanks.stream()
                .map(rank -> toDTO(rank, includeTerminalStats))
                .collect(Collectors.toList());
    }
    
    /**
     * Shorthand method to convert list of entities to list of DTOs with minimal information
     */
    public List<TaxiRankDTO> toDTOList(List<TaxiRank> taxiRanks) {
        return toDTOList(taxiRanks, false);
    }
    
    /**
     * Update a TaxiRank entity from a DTO
     */
    public void updateEntityFromDTO(TaxiRank entity, TaxiRankDTO dto) {
        entity.setName(dto.getName());
        
        // Only update code if explicitly provided
        if (dto.getCode() != null && !dto.getCode().isEmpty()) {
            entity.setCode(dto.getCode());
        }
        
        entity.setDescription(dto.getDescription());
        entity.setAddress(dto.getAddress());
        entity.setCity(dto.getCity());
        entity.setProvince(dto.getProvince());
        entity.setLatitude(dto.getLatitude());
        entity.setLongitude(dto.getLongitude());
        entity.setContactPhone(dto.getContactPhone());
        entity.setContactEmail(dto.getContactEmail());
        entity.setOperatingHours(dto.getOperatingHours());
        entity.setCapacity(dto.getCapacity());
        
        if (dto.getIsActive() != null) {
            entity.setIsActive(dto.getIsActive());
        }
    }
    
    /**
     * Create a new TaxiRank entity from a DTO
     */
    public TaxiRank createEntityFromDTO(TaxiRankDTO dto) {
        TaxiRank entity = new TaxiRank();
        updateEntityFromDTO(entity, dto);
        return entity;
    }
} 