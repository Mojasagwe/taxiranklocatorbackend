package com.taxirank.backend.controllers;

import com.taxirank.backend.dto.ApiResponse;
import com.taxirank.backend.dto.TripDtos.LogTripRequest;
import com.taxirank.backend.dto.TripDtos.StartTripRequest;
import com.taxirank.backend.dto.TripDtos.TripFeedbackRequest;
import com.taxirank.backend.dto.TripDtos.TripResponse;
import com.taxirank.backend.enums.TripStatus;
import com.taxirank.backend.security.UserPrincipal;
import com.taxirank.backend.services.TripService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** The signed-in rider's trip history */
@RestController
@RequestMapping("/api/trips")
@PreAuthorize("hasAuthority('TRIPS_OWN')")
public class TripController {

    @Autowired
    private TripService tripService;

    @GetMapping
    public ResponseEntity<?> list(@AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) TripStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Trips",
                tripService.list(principal.getId(), status).stream().map(TripResponse::from).toList()));
    }

    @GetMapping("/summary")
    public ResponseEntity<?> summary(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Trip summary", tripService.summary(principal.getId())));
    }

    @PostMapping
    public ResponseEntity<?> log(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody LogTripRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Trip saved",
                TripResponse.from(tripService.log(principal.getId(), request))));
    }

    /** The trip in progress, or null data when there's none */
    @GetMapping("/active")
    public ResponseEntity<?> active(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Trip in progress",
                tripService.active(principal.getId()).map(TripResponse::from).orElse(null)));
    }

    @PostMapping("/start")
    public ResponseEntity<?> start(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody StartTripRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Trip started",
                TripResponse.from(tripService.start(principal.getId(), request.terminalId()))));
    }

    @PostMapping("/{id}/departed")
    public ResponseEntity<?> departed(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Departure recorded",
                TripResponse.from(tripService.departed(principal.getId(), id))));
    }

    @PostMapping("/{id}/arrived")
    public ResponseEntity<?> arrived(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Trip saved",
                TripResponse.from(tripService.arrived(principal.getId(), id))));
    }

    @PostMapping("/{id}/feedback")
    public ResponseEntity<?> feedback(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id,
            @Valid @RequestBody TripFeedbackRequest request) {
        tripService.feedback(principal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Thanks for your answers", null));
    }

    @PostMapping("/{id}/taken")
    public ResponseEntity<?> markTaken(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Trip marked as taken",
                TripResponse.from(tripService.markTaken(principal.getId(), id))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        tripService.delete(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Trip removed", null));
    }
}
