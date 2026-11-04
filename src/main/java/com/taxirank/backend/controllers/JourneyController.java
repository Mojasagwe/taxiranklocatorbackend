package com.taxirank.backend.controllers;

import com.taxirank.backend.dto.ApiResponse;
import com.taxirank.backend.services.JourneyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/** Finding taxis to a destination. Needs a login: it takes the rider's location. */
@RestController
@RequestMapping("/api")
@PreAuthorize("isAuthenticated()")
public class JourneyController {

    @Autowired
    private JourneyService journeyService;

    @GetMapping("/journeys")
    public ResponseEntity<?> search(@RequestParam double fromLat, @RequestParam double fromLng,
            @RequestParam double toLat, @RequestParam double toLng,
            @RequestParam(defaultValue = "2") double radiusKm) {
        return ResponseEntity.ok(ApiResponse.success("Journeys",
                journeyService.search(fromLat, fromLng, toLat, toLng, radiusKm, LocalDateTime.now())));
    }

    /** Destinations Travy knows; replaced by Google place search in the app once it has an API key */
    @GetMapping("/places")
    public ResponseEntity<?> places(@RequestParam(name = "q", defaultValue = "") String query) {
        return ResponseEntity.ok(ApiResponse.success("Places", journeyService.places(query)));
    }
}
