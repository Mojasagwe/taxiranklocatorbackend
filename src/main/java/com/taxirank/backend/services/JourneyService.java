package com.taxirank.backend.services;

import com.taxirank.backend.dto.RouteDtos.JourneyResults;
import com.taxirank.backend.dto.RouteDtos.Place;

import java.time.LocalDateTime;
import java.util.List;

/** Finding taxis from where the rider is to where they're going */
public interface JourneyService {
    /** Ranks within radiusKm of the origin with a route to the destination, directly or with one change */
    JourneyResults search(double fromLat, double fromLng, double toLat, double toLng, double radiusKm, LocalDateTime now);

    /** Destinations Travy knows (route ends and ranks) matching the text; the stand-in until Google place search */
    List<Place> places(String query);
}
