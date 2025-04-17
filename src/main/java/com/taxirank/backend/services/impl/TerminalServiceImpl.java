package com.taxirank.backend.services.impl;

import com.taxirank.backend.dto.TerminalDTO;
import com.taxirank.backend.models.TaxiRank;
import com.taxirank.backend.models.Terminal;
import com.taxirank.backend.repositories.TaxiRankRepository;
import com.taxirank.backend.repositories.TerminalRepository;
import com.taxirank.backend.services.TerminalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TerminalServiceImpl implements TerminalService {

    @Autowired
    private TerminalRepository terminalRepository;
    
    @Autowired
    private TaxiRankRepository taxiRankRepository;
    
    @Override
    public List<Terminal> getTerminalsByRankId(Long rankId) {
        return terminalRepository.findByTaxiRankId(rankId);
    }
    
    @Override
    public List<Terminal> getActiveTerminalsByRankId(Long rankId) {
        return terminalRepository.findByTaxiRankIdAndIsActiveTrue(rankId);
    }
    
    @Override
    public List<Terminal> getTerminalsByRankCode(String rankCode) {
        return terminalRepository.findByTaxiRankCode(rankCode);
    }
    
    @Override
    public Terminal getTerminalById(Long terminalId) {
        return terminalRepository.findById(terminalId)
                .orElseThrow(() -> new RuntimeException("Terminal not found with id: " + terminalId));
    }
    
    @Override
    public Terminal getTerminalByIdAndRankId(Long terminalId, Long rankId) {
        return terminalRepository.findByIdAndTaxiRankId(terminalId, rankId)
                .orElseThrow(() -> new RuntimeException("Terminal not found with id: " + terminalId + " for rank: " + rankId));
    }
    
    @Override
    @Transactional
    public Terminal createTerminal(TerminalDTO terminalDTO) {
        TaxiRank taxiRank = taxiRankRepository.findById(terminalDTO.getTaxiRankId())
                .orElseThrow(() -> new RuntimeException("Taxi Rank not found with id: " + terminalDTO.getTaxiRankId()));
        
        Terminal terminal = new Terminal();
        mapDtoToEntity(terminal, terminalDTO);
        terminal.setTaxiRank(taxiRank);
        
        return terminalRepository.save(terminal);
    }
    
    @Override
    @Transactional
    public Terminal updateTerminal(Long terminalId, TerminalDTO terminalDTO) {
        Terminal terminal = getTerminalById(terminalId);
        
        // If changing the taxi rank
        if (!terminal.getTaxiRank().getId().equals(terminalDTO.getTaxiRankId())) {
            TaxiRank newTaxiRank = taxiRankRepository.findById(terminalDTO.getTaxiRankId())
                    .orElseThrow(() -> new RuntimeException("Taxi Rank not found with id: " + terminalDTO.getTaxiRankId()));
            terminal.setTaxiRank(newTaxiRank);
        }
        
        mapDtoToEntity(terminal, terminalDTO);
        return terminalRepository.save(terminal);
    }
    
    @Override
    @Transactional
    public void deleteTerminal(Long terminalId) {
        Terminal terminal = getTerminalById(terminalId);
        terminalRepository.delete(terminal);
    }
    
    @Override
    @Transactional
    public Terminal setTerminalStatus(Long terminalId, boolean isActive) {
        Terminal terminal = getTerminalById(terminalId);
        terminal.setActive(isActive);
        return terminalRepository.save(terminal);
    }
    
    @Override
    public Long countTerminalsByRankId(Long rankId) {
        return terminalRepository.countByTaxiRankId(rankId);
    }
    
    @Override
    public Long countActiveTerminalsByRankId(Long rankId) {
        return terminalRepository.countByTaxiRankIdAndIsActiveTrue(rankId);
    }
    
    /**
     * Map DTO values to entity
     */
    private void mapDtoToEntity(Terminal terminal, TerminalDTO dto) {
        terminal.setName(dto.getName());
        terminal.setFare(dto.getFare());
        terminal.setTravelTime(dto.getTravelTime());
        terminal.setDistance(dto.getDistance());
        
        // Optional fields
        if (dto.getDepartureSchedule() != null) {
            terminal.setDepartureSchedule(dto.getDepartureSchedule());
        }
        
        if (dto.getOperatingDays() != null) {
            terminal.setOperatingDays(dto.getOperatingDays());
        }
        
        if (dto.getIsActive() != null) {
            terminal.setActive(dto.getIsActive());
        }
    }
} 