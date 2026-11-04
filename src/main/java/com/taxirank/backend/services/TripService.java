package com.taxirank.backend.services;

import com.taxirank.backend.dto.TripDtos.LogTripRequest;
import com.taxirank.backend.dto.TripDtos.TripFeedbackRequest;
import com.taxirank.backend.dto.TripDtos.TripSummary;
import com.taxirank.backend.enums.TripStatus;
import com.taxirank.backend.models.Trip;

import java.util.List;
import java.util.Optional;

/** A rider's own trip history. Every method is scoped to userId: other riders' trips are "not found". */
public interface TripService {
    /** Newest first; status optional. The trip in progress (ACTIVE) is left out unless asked for. */
    List<Trip> list(Long userId, TripStatus status);

    Optional<Trip> active(Long userId);

    /** "I'm in the taxi": one trip in progress at a time */
    Trip start(Long userId, Long terminalId);

    /** "We've left the rank" */
    Trip departed(Long userId, Long tripId);

    /** "I've arrived": the trip becomes TAKEN */
    Trip arrived(Long userId, Long tripId);

    /** The after-trip answers; once per trip */
    void feedback(Long userId, Long tripId, TripFeedbackRequest request);

    Trip log(Long userId, LogTripRequest request);

    Trip markTaken(Long userId, Long tripId);

    void delete(Long userId, Long tripId);

    TripSummary summary(Long userId);
}
