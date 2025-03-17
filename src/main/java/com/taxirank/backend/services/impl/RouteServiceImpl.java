package com.taxirank.backend.services.impl;

import com.taxirank.backend.models.Route;
import com.taxirank.backend.models.Rank;
import com.taxirank.backend.dto.RouteDTO;
import com.taxirank.backend.repositories.RouteRepository;
import com.taxirank.backend.repositories.RankRepository;
import com.taxirank.backend.services.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RouteServiceImpl implements RouteService {

    @Autowired
    private RouteRepository routeRepository;
    
    @Autowired
    private RankRepository rankRepository;

    @Override
    public Route createRoute(RouteDTO routeDTO) {
        Route route = new Route();
        updateRouteFromDTO(route, routeDTO);
        return routeRepository.save(route);
    }

    @Override
    public Route updateRoute(Long id, RouteDTO routeDTO) {
        Route route = routeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Route not found"));
        updateRouteFromDTO(route, routeDTO);
        return routeRepository.save(route);
    }

    @Override
    public void deleteRoute(Long id) {
        routeRepository.deleteById(id);
    }

    @Override
    public Route getRoute(Long id) {
        return routeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Route not found"));
    }

    @Override
    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    @Override
    public List<Route> findRoutesBetweenRanks(Long sourceRankId, Long destinationRankId) {
        Rank sourceRank = rankRepository.findById(sourceRankId)
            .orElseThrow(() -> new RuntimeException("Source rank not found"));
        Rank destinationRank = rankRepository.findById(destinationRankId)
            .orElseThrow(() -> new RuntimeException("Destination rank not found"));
            
        return routeRepository.findRoutesBetweenRanks(sourceRank, destinationRank);
    }

    @Override
    public List<Route> findRoutesWithinDistance(Double maxDistance) {
        return routeRepository.findRoutesWithinDistance(maxDistance);
    }

    @Override
    public List<Route> findOptimalRoutes(Long sourceRankId, Long destinationRankId) {
        // TODO: Implement route optimization algorithm
        return findRoutesBetweenRanks(sourceRankId, destinationRankId);
    }

    private void updateRouteFromDTO(Route route, RouteDTO routeDTO) {
        Rank sourceRank = rankRepository.findById(routeDTO.getSourceRankId())
            .orElseThrow(() -> new RuntimeException("Source rank not found"));
        Rank destinationRank = rankRepository.findById(routeDTO.getDestinationRankId())
            .orElseThrow(() -> new RuntimeException("Destination rank not found"));

        route.setSourceRank(sourceRank);
        route.setDestinationRank(destinationRank);
        route.setFare(routeDTO.getFare());
        route.setEstimatedDuration(routeDTO.getEstimatedDuration());
        route.setDistance(routeDTO.getDistance());
        route.setTrafficStatus(routeDTO.getTrafficStatus());
        route.setPeakHours(routeDTO.getPeakHours());
    }
} 