package com.taxirank.backend.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taxirank.backend.dto.ApiResponse;
import com.taxirank.backend.models.TaxiRank;
import com.taxirank.backend.services.TaxiRankService;

/**
 * Public controller for accessing taxi ranks
 * This controller is intended for public access and doesn't require authentication
 */
@RestController
@RequestMapping("/api/ranks")
public class RankController {

    @Autowired
    private TaxiRankService taxiRankService;
    
    // Public endpoint to view all ranks
    @GetMapping
    public ResponseEntity<?> getAllRanks() {
        List<TaxiRank> ranks = taxiRankService.getAllRanks();
        return ResponseEntity.ok(ApiResponse.success("All taxi ranks retrieved", ranks));
    }
    
    // Public endpoint to view a specific rank
    @GetMapping("/{id}")
    public ResponseEntity<?> getRankById(@PathVariable Long id) {
        try {
            TaxiRank taxiRank = taxiRankService.getRankById(id);
            return ResponseEntity.ok(ApiResponse.success("Taxi rank retrieved successfully", taxiRank));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Public endpoint to delete all ranks (for development/testing)
    @DeleteMapping("/all")
    public ResponseEntity<?> deleteAllRanks() {
        try {
            List<TaxiRank> ranks = taxiRankService.getAllRanks();
            for (TaxiRank rank : ranks) {
                taxiRankService.deleteRank(rank.getId());
            }
            return ResponseEntity.ok(ApiResponse.success("All taxi ranks deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete all taxi ranks: " + e.getMessage()));
        }
    }
    
    // Public endpoint to create a new rank (for development/testing)
    @PostMapping
    public ResponseEntity<?> createRank(@RequestBody TaxiRank taxiRank) {
        try {
            TaxiRank createdRank = taxiRankService.createRank(taxiRank);
            return ResponseEntity.ok(ApiResponse.success("Taxi rank created successfully", createdRank));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create taxi rank: " + e.getMessage()));
        }
    }
} 