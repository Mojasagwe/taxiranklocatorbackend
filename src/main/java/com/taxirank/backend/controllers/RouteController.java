package com.taxirank.backend.controllers;

import com.taxirank.backend.models.Route;
import com.taxirank.backend.dto.RouteDTO;
import com.taxirank.backend.services.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    @Autowired
    private RouteService routeService;

    @PostMapping
    public ResponseEntity<Route> createRoute(@RequestBody RouteDTO routeDTO) {
        return ResponseEntity.ok(routeService.createRoute(routeDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Route> updateRoute(@PathVariable Long id, @RequestBody RouteDTO routeDTO) {
        return ResponseEntity.ok(routeService.updateRoute(id, routeDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoute(@PathVariable Long id) {
        routeService.deleteRoute(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Route> getRoute(@PathVariable Long id) {
        return ResponseEntity.ok(routeService.getRoute(id));
    }

    @GetMapping
    public ResponseEntity<List<Route>> getAllRoutes() {
        return ResponseEntity.ok(routeService.getAllRoutes());
    }

    @GetMapping("/between")
    public ResponseEntity<List<Route>> findRoutesBetweenRanks(
            @RequestParam Long sourceRankId,
            @RequestParam Long destinationRankId) {
        return ResponseEntity.ok(routeService.findRoutesBetweenRanks(sourceRankId, destinationRankId));
    }

    @GetMapping("/within")
    public ResponseEntity<List<Route>> findRoutesWithinDistance(@RequestParam Double maxDistance) {
        return ResponseEntity.ok(routeService.findRoutesWithinDistance(maxDistance));
    }

    @GetMapping("/optimal")
    public ResponseEntity<List<Route>> findOptimalRoutes(
            @RequestParam Long sourceRankId,
            @RequestParam Long destinationRankId) {
        return ResponseEntity.ok(routeService.findOptimalRoutes(sourceRankId, destinationRankId));
    }
} 