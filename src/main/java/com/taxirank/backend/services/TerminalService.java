package com.taxirank.backend.services;

import com.taxirank.backend.dto.TerminalDTO;
import com.taxirank.backend.models.Terminal;

import java.util.List;

/**
 * Service for managing Terminal entities
 */
public interface TerminalService {
    
    /**
     * Get all terminals for a taxi rank
     */
    List<Terminal> getTerminalsByRankId(Long rankId);
    
    /**
     * Get all active terminals for a taxi rank
     */
    List<Terminal> getActiveTerminalsByRankId(Long rankId);
    
    /**
     * Get all terminals for a taxi rank by rank code
     */
    List<Terminal> getTerminalsByRankCode(String rankCode);
    
    /**
     * Get a specific terminal by its ID
     */
    Terminal getTerminalById(Long terminalId);
    
    /**
     * Get a specific terminal by its ID and rank ID
     */
    Terminal getTerminalByIdAndRankId(Long terminalId, Long rankId);
    
    /**
     * Create a new terminal
     */
    Terminal createTerminal(TerminalDTO terminalDTO);
    
    /**
     * Update an existing terminal
     */
    Terminal updateTerminal(Long terminalId, TerminalDTO terminalDTO);
    
    /**
     * Delete a terminal
     */
    void deleteTerminal(Long terminalId);
    
    /**
     * Activate or deactivate a terminal
     */
    Terminal setTerminalStatus(Long terminalId, boolean isActive);
    
    /**
     * Count terminals for a taxi rank
     */
    Long countTerminalsByRankId(Long rankId);
    
    /**
     * Count active terminals for a taxi rank
     */
    Long countActiveTerminalsByRankId(Long rankId);
} 