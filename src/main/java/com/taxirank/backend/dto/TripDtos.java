package com.taxirank.backend.dto;

import com.taxirank.backend.enums.QueueReport;
import com.taxirank.backend.enums.RideRating;
import com.taxirank.backend.enums.TripStatus;
import com.taxirank.backend.models.Terminal;
import com.taxirank.backend.models.Trip;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Request/response bodies for /api/trips and a rank's destinations */
public final class TripDtos {
    private TripDtos() {
    }

    /** tripDate defaults to now. A taken trip can't be in the future. */
    public record LogTripRequest(@NotNull Long terminalId, @NotNull TripStatus status, LocalDateTime tripDate) {
    }

    public record RankRef(Long id, String code, String name) {
    }

    public record TripResponse(Long id, RankRef rank, Long terminalId, String destination, BigDecimal fare,
            TripStatus status, LocalDateTime tripDate, String bay, LocalDateTime boardedAt, LocalDateTime departedAt,
            LocalDateTime arrivedAt) {
        public static TripResponse from(Trip trip) {
            return new TripResponse(trip.getId(),
                    new RankRef(trip.getRank().getId(), trip.getRank().getCode(), trip.getRank().getName()),
                    trip.getTerminal() == null ? null : trip.getTerminal().getId(),
                    trip.getDestination(), trip.getFare(), trip.getStatus(), trip.getTripDate(),
                    trip.getTerminal() == null ? null : trip.getTerminal().getBay(),
                    trip.getBoardedAt(), trip.getDepartedAt(), trip.getArrivedAt());
        }
    }

    /** "I'm in the taxi" on this route */
    public record StartTripRequest(@NotNull Long terminalId) {
    }

    /** All optional; farePaid only when it differed from the listed fare (or to confirm it) */
    public record TripFeedbackRequest(
            @Positive @DecimalMax("5000") BigDecimal farePaid,
            QueueReport queue,
            RideRating rating) {
    }

    /** The rider's stats on their profile; counts only taken trips */
    public record TripSummary(long tripsTaken, BigDecimal spentThisMonth, long ranksUsed) {
    }

    /** A destination served from a rank: what a rider picks when logging a trip */
    public record DestinationResponse(Long id, String name, Double fare, String travelTime, String distance, String bay) {
        public static DestinationResponse from(Terminal terminal) {
            return new DestinationResponse(terminal.getId(), terminal.getName(), terminal.getFare(),
                    terminal.getTravelTime(), terminal.getDistance(), terminal.getBay());
        }
    }
}
