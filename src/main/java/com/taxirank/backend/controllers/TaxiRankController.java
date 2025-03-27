package com.taxirank.backend.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

@RestController
@RequestMapping("/api/taxi-ranks")
public class TaxiRankController {

    @Autowired
    private TaxiRankService taxiRankService;
    
    // Public endpoint to view all ranks
    @GetMapping
    public ResponseEntity<List<TaxiRank>> getAllRanks() {
        return ResponseEntity.ok(taxiRankService.getAllRanks());
    }
    
    // Public endpoint to view a specific rank
    @GetMapping("/{id}")
    public ResponseEntity<?> getRankById(@PathVariable Long id) {
        try {
            TaxiRank taxiRank = taxiRankService.getRankById(id);
            return ResponseEntity.ok(taxiRank);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Taxi rank not found: " + e.getMessage()));
        }
    }
    
    // Admin only - create a new rank
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createRank(@RequestBody TaxiRank taxiRank) {
        try {
            TaxiRank createdRank = taxiRankService.createRank(taxiRank);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Taxi rank created successfully", createdRank));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create taxi rank: " + e.getMessage()));
        }
    }
    
    // Admin only - update a rank
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateRank(@PathVariable Long id, @RequestBody TaxiRank taxiRankDetails) {
        try {
            TaxiRank updatedRank = taxiRankService.updateRank(id, taxiRankDetails);
            return ResponseEntity.ok(ApiResponse.success("Taxi rank updated successfully", updatedRank));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update taxi rank: " + e.getMessage()));
        }
    }
    
    // Admin only - delete a rank
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteRank(@PathVariable Long id) {
        try {
            taxiRankService.deleteRank(id);
            return ResponseEntity.ok(ApiResponse.success("Taxi rank deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete taxi rank: " + e.getMessage()));
        }
    }
}