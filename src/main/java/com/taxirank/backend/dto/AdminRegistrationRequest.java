package com.taxirank.backend.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for admin self-registration with rank selection.
 */
@Data
public class AdminRegistrationRequest {
    // User information
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phoneNumber;
    
    // Admin specific information
    private List<String> selectedRankCodes;
    
    // For backward compatibility
    private List<String> selectedRankIds;
    private List<String> rankIds;
    
    // Helper method to get the rank codes, prioritizing selectedRankCodes
    public List<String> getEffectiveRankCodes() {
        if (selectedRankCodes != null && !selectedRankCodes.isEmpty()) {
            return new ArrayList<>(selectedRankCodes);
        }
        
        return new ArrayList<>();
    }
    
    // Legacy helper method for ID-based references - retained for backward compatibility
    public List<Long> getEffectiveRankIds() {
        List<Long> result = new ArrayList<>();
        
        // First try selectedRankIds
        if (selectedRankIds != null && !selectedRankIds.isEmpty()) {
            for (String id : selectedRankIds) {
                try {
                    result.add(Long.parseLong(id));
                } catch (NumberFormatException e) {
                    // Skip invalid IDs
                }
            }
        }
        
        // If no results from selectedRankIds, try rankIds
        if (result.isEmpty() && rankIds != null && !rankIds.isEmpty()) {
            for (String id : rankIds) {
                try {
                    result.add(Long.parseLong(id));
                } catch (NumberFormatException e) {
                    // Skip invalid IDs
                }
            }
        }
        
        return result;
    }
}