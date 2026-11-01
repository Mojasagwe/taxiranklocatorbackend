package com.taxirank.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
