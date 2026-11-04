package com.taxirank.backend.utils;

import com.taxirank.backend.dto.TerminalDTO;
import com.taxirank.backend.models.Terminal;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for mapping between Terminal entities and DTOs
 */
public class TerminalMapper {

    /**
     * Convert a Terminal entity to a TerminalDTO
     */
    public static TerminalDTO toDTO(Terminal terminal) {
        if (terminal == null) {
            return null;
        }
        
        TerminalDTO dto = new TerminalDTO();
        dto.setId(terminal.getId());
        dto.setName(terminal.getName());
        dto.setFare(terminal.getFare());
        dto.setTravelTime(terminal.getTravelTime());
        dto.setDistance(terminal.getDistance());
        dto.setDepartureSchedule(terminal.getDepartureSchedule());
        dto.setOperatingDays(terminal.getOperatingDays());
        dto.setIsActive(terminal.isActive());
        dto.setDestinationLatitude(terminal.getDestinationLatitude());
        dto.setDestinationLongitude(terminal.getDestinationLongitude());
        dto.setDestinationPlaceId(terminal.getDestinationPlaceId());
        dto.setBay(terminal.getBay());
        dto.setIntervalMinutes(terminal.getIntervalMinutes());
        dto.setRunningToday(terminal.isRunningOn(LocalDate.now()));
        dto.setFirstTaxi(terminal.getFirstTaxi());
        dto.setLastTaxiWeekday(terminal.getLastTaxiWeekday());
        dto.setLastTaxiSaturday(terminal.getLastTaxiSaturday());
        dto.setLastTaxiSunday(terminal.getLastTaxiSunday());
        if (terminal.getEndsAtRank() != null) {
            dto.setEndsAtRankId(terminal.getEndsAtRank().getId());
            dto.setEndsAtRankName(terminal.getEndsAtRank().getName());
        }
        
        // Include taxi rank information
        if (terminal.getTaxiRank() != null) {
            dto.setTaxiRankId(terminal.getTaxiRank().getId());
            dto.setTaxiRankName(terminal.getTaxiRank().getName());
            dto.setTaxiRankCode(terminal.getTaxiRank().getCode());
        }
        
        return dto;
    }
    
    /**
     * Convert a list of Terminal entities to a list of TerminalDTOs
     */
    public static List<TerminalDTO> toDTOList(List<Terminal> terminals) {
        return terminals.stream()
                .map(TerminalMapper::toDTO)
                .collect(Collectors.toList());
    }
} 