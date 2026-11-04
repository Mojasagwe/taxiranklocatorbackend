package com.taxirank.backend.controllers;

import com.taxirank.backend.dto.ApiResponse;
import com.taxirank.backend.dto.TerminalDTO;
import com.taxirank.backend.models.Terminal;
import com.taxirank.backend.services.TerminalService;
import com.taxirank.backend.utils.TerminalMapper;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/taxi-ranks/{rankId}/terminals")
public class TerminalController {

    @Autowired
    private TerminalService terminalService;
    
    /**
     * Get all terminals for a taxi rank
     */
    @GetMapping
    public ResponseEntity<?> getTerminalsByRankId(@PathVariable Long rankId, 
                                                 @RequestParam(required = false) Boolean onlyActive) {
        List<Terminal> terminals;
        
        if (onlyActive != null && onlyActive) {
            terminals = terminalService.getActiveTerminalsByRankId(rankId);
        } else {
            terminals = terminalService.getTerminalsByRankId(rankId);
        }
        
        return ResponseEntity.ok(ApiResponse.success(
                "Terminals retrieved successfully", TerminalMapper.toDTOList(terminals)));
    }
    
    /**
     * Get a specific terminal
     */
    @GetMapping("/{terminalId}")
    public ResponseEntity<?> getTerminalById(@PathVariable Long rankId, @PathVariable Long terminalId) {
        try {
            Terminal terminal = terminalService.getTerminalByIdAndRankId(terminalId, rankId);
            return ResponseEntity.ok(ApiResponse.success(
                    "Terminal retrieved successfully", TerminalMapper.toDTO(terminal)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Terminal not found: " + e.getMessage()));
        }
    }
    
    /**
     * Create a new terminal
     */
    @PostMapping
    @PreAuthorize("@rankAccess.canManage(authentication, #rankId)")
    public ResponseEntity<?> createTerminal(@PathVariable Long rankId, 
                                           @Valid @RequestBody TerminalDTO terminalDTO,
                                           Authentication authentication) {
        try {
            terminalDTO.setTaxiRankId(rankId);
            Terminal terminal = terminalService.createTerminal(terminalDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                    "Terminal created successfully", TerminalMapper.toDTO(terminal)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create terminal: " + e.getMessage()));
        }
    }
    
    /**
     * Update a terminal
     */
    @PutMapping("/{terminalId}")
    @PreAuthorize("@rankAccess.canManage(authentication, #rankId)")
    public ResponseEntity<?> updateTerminal(@PathVariable Long rankId, 
                                           @PathVariable Long terminalId, 
                                           @Valid @RequestBody TerminalDTO terminalDTO,
                                           Authentication authentication) {
        try {
            // The terminal must belong to the rank in the path, or one rank could edit another's terminals
            if (belongsToRank(terminalId, rankId)) {
                terminalDTO.setTaxiRankId(rankId);
                Terminal terminal = terminalService.updateTerminal(terminalId, terminalDTO);
                return ResponseEntity.ok(ApiResponse.success(
                        "Terminal updated successfully", TerminalMapper.toDTO(terminal)));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Terminal not found for this rank"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update terminal: " + e.getMessage()));
        }
    }
    
    /**
     * Delete a terminal
     */
    @DeleteMapping("/{terminalId}")
    @PreAuthorize("@rankAccess.canManage(authentication, #rankId)")
    public ResponseEntity<?> deleteTerminal(@PathVariable Long rankId, 
                                           @PathVariable Long terminalId,
                                           Authentication authentication) {
        try {
            // The terminal must belong to the rank in the path, or one rank could edit another's terminals
            if (belongsToRank(terminalId, rankId)) {
                terminalService.deleteTerminal(terminalId);
                return ResponseEntity.ok(ApiResponse.success("Terminal deleted successfully", null));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Terminal not found for this rank"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete terminal: " + e.getMessage()));
        }
    }
    
    /**
     * Activate or deactivate a terminal
     */
    @PatchMapping("/{terminalId}/status")
    @PreAuthorize("@rankAccess.canManage(authentication, #rankId)")
    public ResponseEntity<?> updateTerminalStatus(@PathVariable Long rankId, 
                                                 @PathVariable Long terminalId, 
                                                 @RequestBody Map<String, Boolean> status,
                                                 Authentication authentication) {
        try {
            // The terminal must belong to the rank in the path, or one rank could edit another's terminals
            if (belongsToRank(terminalId, rankId)) {
                Boolean isActive = status.get("isActive");
                if (isActive == null) {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("isActive field is required"));
                }
                
                Terminal terminal = terminalService.setTerminalStatus(terminalId, isActive);
                return ResponseEntity.ok(ApiResponse.success(
                        "Terminal status updated successfully", TerminalMapper.toDTO(terminal)));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Terminal not found for this rank"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update terminal status: " + e.getMessage()));
        }
    }
    
    /**
     * Get terminal statistics for a rank
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getTerminalStats(@PathVariable Long rankId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTerminals", terminalService.countTerminalsByRankId(rankId));
        stats.put("activeTerminals", terminalService.countActiveTerminalsByRankId(rankId));
        
        return ResponseEntity.ok(ApiResponse.success("Terminal statistics retrieved", stats));
    }
    
    private boolean belongsToRank(Long terminalId, Long rankId) {
        try {
            terminalService.getTerminalByIdAndRankId(terminalId, rankId);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
} 