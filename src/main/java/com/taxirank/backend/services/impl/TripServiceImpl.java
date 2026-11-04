package com.taxirank.backend.services.impl;

import com.taxirank.backend.dto.TripDtos.LogTripRequest;
import com.taxirank.backend.dto.TripDtos.TripFeedbackRequest;
import com.taxirank.backend.dto.TripDtos.TripSummary;
import com.taxirank.backend.enums.TripStatus;
import com.taxirank.backend.exceptions.ConflictException;
import com.taxirank.backend.exceptions.NotFoundException;
import com.taxirank.backend.models.Terminal;
import com.taxirank.backend.models.Trip;
import com.taxirank.backend.models.TripFeedback;
import com.taxirank.backend.repositories.TripFeedbackRepository;
import com.taxirank.backend.repositories.TerminalRepository;
import com.taxirank.backend.repositories.TripRepository;
import com.taxirank.backend.repositories.UserRepository;
import com.taxirank.backend.services.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TripServiceImpl implements TripService {

    // Phone clocks drift; allow a taken trip a few minutes "in the future"
    private static final long CLOCK_SKEW_MINUTES = 5;
    private static final long MAX_PLAN_AHEAD_DAYS = 366;
    static final long STALE_ACTIVE_HOURS = 6;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private TerminalRepository terminalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TripFeedbackRepository tripFeedbackRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Trip> list(Long userId, TripStatus status) {
        return status == null
                ? tripRepository.findByUserIdAndStatusInOrderByTripDateDescIdDesc(userId, List.of(TripStatus.PLANNED, TripStatus.TAKEN))
                : tripRepository.findByUserIdAndStatusOrderByTripDateDescIdDesc(userId, status);
    }

    @Override
    @Transactional
    public Optional<Trip> active(Long userId) {
        return tripRepository.findFirstByUserIdAndStatus(userId, TripStatus.ACTIVE).filter(t -> !closeIfStale(t));
    }

    @Override
    @Transactional
    public Trip start(Long userId, Long terminalId) {
        if (active(userId).isPresent()) {
            throw new ConflictException("You already have a trip in progress");
        }
        Terminal terminal = activeTerminal(terminalId);
        LocalDateTime now = LocalDateTime.now();
        Trip trip = newTrip(userId, terminal, TripStatus.ACTIVE, now);
        trip.setBoardedAt(now);
        return tripRepository.save(trip);
    }

    @Override
    @Transactional
    public Trip departed(Long userId, Long tripId) {
        Trip trip = inProgress(userId, tripId);
        if (trip.getDepartedAt() != null) {
            throw new IllegalArgumentException("This taxi has already left");
        }
        trip.setDepartedAt(LocalDateTime.now());
        return tripRepository.save(trip);
    }

    @Override
    @Transactional
    public Trip arrived(Long userId, Long tripId) {
        Trip trip = inProgress(userId, tripId);
        trip.setArrivedAt(LocalDateTime.now());
        trip.setStatus(TripStatus.TAKEN);
        return tripRepository.save(trip);
    }

    @Override
    @Transactional
    public void feedback(Long userId, Long tripId, TripFeedbackRequest request) {
        Trip trip = own(userId, tripId);
        if (trip.getStatus() != TripStatus.TAKEN) {
            throw new IllegalArgumentException("Answer the questions after the trip");
        }
        if (tripFeedbackRepository.existsByTripId(tripId)) {
            throw new ConflictException("You already answered for this trip");
        }
        TripFeedback feedback = new TripFeedback();
        feedback.setTrip(trip);
        feedback.setTerminal(trip.getTerminal());
        feedback.setFarePaid(request.farePaid());
        feedback.setQueue(request.queue());
        feedback.setRating(request.rating());
        tripFeedbackRepository.save(feedback);
    }

    @Override
    @Transactional
    public Trip log(Long userId, LogTripRequest request) {
        Terminal terminal = activeTerminal(request.terminalId());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime when = request.tripDate() == null ? now : request.tripDate();
        if (request.status() == TripStatus.TAKEN && when.isAfter(now.plusMinutes(CLOCK_SKEW_MINUTES))) {
            throw new IllegalArgumentException("A taken trip can't be in the future");
        }
        if (when.isAfter(now.plusDays(MAX_PLAN_AHEAD_DAYS))) {
            throw new IllegalArgumentException("Trips can be planned up to a year ahead");
        }

        return tripRepository.save(newTrip(userId, terminal, request.status(), when));
    }

    @Override
    @Transactional
    public Trip markTaken(Long userId, Long tripId) {
        Trip trip = own(userId, tripId);
        if (trip.getStatus() != TripStatus.PLANNED) {
            throw new IllegalArgumentException("Only a planned trip can be marked as taken");
        }
        trip.setStatus(TripStatus.TAKEN);
        if (trip.getTripDate().isAfter(LocalDateTime.now())) {
            trip.setTripDate(LocalDateTime.now());
        }
        return tripRepository.save(trip);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long tripId) {
        tripRepository.delete(own(userId, tripId));
    }

    @Override
    @Transactional(readOnly = true)
    public TripSummary summary(Long userId) {
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        return new TripSummary(
                tripRepository.countByUserIdAndStatus(userId, TripStatus.TAKEN),
                tripRepository.sumFare(userId, TripStatus.TAKEN, monthStart, monthStart.plusMonths(1)),
                tripRepository.countRanks(userId, TripStatus.TAKEN));
    }

    private Terminal activeTerminal(Long terminalId) {
        return terminalRepository.findById(terminalId)
                .filter(Terminal::isActive)
                .filter(t -> !Boolean.FALSE.equals(t.getTaxiRank().getIsActive()))
                .orElseThrow(() -> new NotFoundException("Destination not found"));
    }

    // The destination name and fare are copied, so history stays right when the rank changes them
    private Trip newTrip(Long userId, Terminal terminal, TripStatus status, LocalDateTime when) {
        Trip trip = new Trip();
        trip.setUser(userRepository.getReferenceById(userId));
        trip.setRank(terminal.getTaxiRank());
        trip.setTerminal(terminal);
        trip.setDestination(terminal.getName());
        trip.setFare(BigDecimal.valueOf(terminal.getFare()));
        trip.setStatus(status);
        trip.setTripDate(when);
        return trip;
    }

    private Trip inProgress(Long userId, Long tripId) {
        Trip trip = own(userId, tripId);
        if (trip.getStatus() != TripStatus.ACTIVE || closeIfStale(trip)) {
            throw new IllegalArgumentException("This trip isn't in progress");
        }
        return trip;
    }

    // Riders forget to tap "arrived". After this long, the trip counts as taken without an arrival time.
    private boolean closeIfStale(Trip trip) {
        if (trip.getBoardedAt().isAfter(LocalDateTime.now().minusHours(STALE_ACTIVE_HOURS))) {
            return false;
        }
        trip.setStatus(TripStatus.TAKEN);
        tripRepository.save(trip);
        return true;
    }

    private Trip own(Long userId, Long tripId) {
        return tripRepository.findByIdAndUserId(tripId, userId)
                .orElseThrow(() -> new NotFoundException("Trip not found"));
    }
}
