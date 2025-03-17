package com.taxirank.backend.repositories;

import com.taxirank.backend.models.Route;
import com.taxirank.backend.models.Rank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    List<Route> findBySourceRank(Rank sourceRank);
    List<Route> findByDestinationRank(Rank destinationRank);
    
    @Query("SELECT r FROM Route r WHERE r.sourceRank = :sourceRank AND r.destinationRank = :destinationRank")
    List<Route> findRoutesBetweenRanks(Rank sourceRank, Rank destinationRank);
    
    @Query("SELECT r FROM Route r WHERE r.distance <= :maxDistance")
    List<Route> findRoutesWithinDistance(Double maxDistance);
} 