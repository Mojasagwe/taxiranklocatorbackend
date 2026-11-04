package com.taxirank.backend.controllers;

import com.taxirank.backend.dto.TripDtos.DestinationResponse;
import com.taxirank.backend.enums.DayType;
import com.taxirank.backend.models.TaxiRank;
import com.taxirank.backend.models.Terminal;
import com.taxirank.backend.services.RouteInsightsService;
import com.taxirank.backend.repositories.TerminalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taxirank.backend.dto.ApiResponse;
import com.taxirank.backend.exceptions.NotFoundException;
import com.taxirank.backend.repositories.TaxiRankRepository;
import com.taxirank.backend.utils.TaxiRankMapper;

/**
 * Public, read-only view of taxi ranks. Changes go through /api/taxi-ranks.
 */
@RestController
@RequestMapping("/api/ranks")
public class RankController {

    @Autowired
    private TaxiRankRepository taxiRankRepository;

    @Autowired
    private TaxiRankMapper rankMapper;

    @Autowired
    private TerminalRepository terminalRepository;

    @Autowired
    private RouteInsightsService insightsService;

    @GetMapping
    public ResponseEntity<?> getAllRanks() {
        return ResponseEntity.ok(ApiResponse.success("All taxi ranks retrieved",
                rankMapper.toDTOList(taxiRankRepository.findAll())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRankById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Taxi rank retrieved successfully",
                rankMapper.toDTO(taxiRankRepository.findById(id).orElseThrow(() -> new NotFoundException("Taxi rank not found")))));
    }

    /**
     * Riders' trends for one route: typical wait by hour, latest departure seen, and the rank's last taxi.
     * Public like the rest of /api/ranks; only aggregates, never individual trips.
     */
    @GetMapping("/{id}/destinations/{terminalId}/insights")
    public ResponseEntity<?> getInsights(@PathVariable Long id, @PathVariable Long terminalId,
            @RequestParam(required = false) DayType day) {
        Terminal terminal = terminalRepository.findByIdAndTaxiRankId(terminalId, id)
                .filter(Terminal::isActive)
                .orElseThrow(() -> new NotFoundException("Destination not found"));
        LocalDateTime now = LocalDateTime.now();
        return ResponseEntity.ok(ApiResponse.success("Route insights",
                insightsService.insights(terminal, day != null ? day : DayType.of(now.toLocalDate()), now)));
    }

    /** Active destinations from an active rank, with fares: what a rider picks when logging a trip */
    @GetMapping("/{id}/destinations")
    public ResponseEntity<?> getDestinations(@PathVariable Long id) {
        TaxiRank rank = taxiRankRepository.findById(id)
                .filter(r -> !Boolean.FALSE.equals(r.getIsActive()))
                .orElseThrow(() -> new NotFoundException("Taxi rank not found"));
        return ResponseEntity.ok(ApiResponse.success("Destinations",
                terminalRepository.findByTaxiRankIdAndIsActiveTrue(rank.getId()).stream()
                        .map(DestinationResponse::from).toList()));
    }
}
