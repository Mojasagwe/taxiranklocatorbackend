package com.taxirank.backend.repositories;

import com.taxirank.backend.models.Route;
import com.taxirank.backend.models.TaxiRank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    List<Route> findBySourceRank(TaxiRank sourceRank);
    List<Route> findByDestinationRank(TaxiRank destinationRank);
    
    @Query("SELECT r FROM Route r WHERE r.sourceRank = :sourceRank AND r.destinationRank = :destinationRank")
    List<Route> findRoutesBetweenRanks(TaxiRank sourceRank, TaxiRank destinationRank);
    
    @Query("SELECT r FROM Route r WHERE r.distance <= :maxDistance")
    List<Route> findRoutesWithinDistance(Double maxDistance);
} 