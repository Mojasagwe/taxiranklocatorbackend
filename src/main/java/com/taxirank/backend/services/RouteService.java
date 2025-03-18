package com.taxirank.backend.services;

import com.taxirank.backend.models.Route;
import com.taxirank.backend.dto.RouteDTO;
import java.util.List;

public interface RouteService {
    Route createRoute(RouteDTO routeDTO);
    Route updateRoute(Long id, RouteDTO routeDTO);
    void deleteRoute(Long id);
    Route getRoute(Long id);
    List<Route> getAllRoutes();
    List<Route> findRoutesBetweenRanks(Long sourceRankId, Long destinationRankId);
    List<Route> findRoutesWithinDistance(Double maxDistance);
    List<Route> findOptimalRoutes(Long sourceRankId, Long destinationRankId); 
} 