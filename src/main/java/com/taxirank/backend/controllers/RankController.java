package com.taxirank.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.taxirank.backend.models.Rank;
import com.taxirank.backend.repositories.RankRepository;
import java.util.List;

@RestController
@RequestMapping("/api/ranks")
public class RankController {

    @Autowired
    private RankRepository rankRepository;
    
    @GetMapping
    public List<Rank> getAllRanks() {
        return rankRepository.findAll();
    }
    
    @PostMapping
    public Rank createRank(@RequestBody Rank rank) {
        return rankRepository.save(rank);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Rank> getRankById(@PathVariable Long id) {
        return rankRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}