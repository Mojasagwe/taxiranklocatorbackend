package com.taxirank.backend.services.impl;

import com.taxirank.backend.dto.RouteDtos.FareReport;
import com.taxirank.backend.dto.RouteDtos.HourlyWait;
import com.taxirank.backend.dto.RouteDtos.RouteInsights;
import com.taxirank.backend.enums.DayType;
import com.taxirank.backend.models.Terminal;
import com.taxirank.backend.models.Trip;
import com.taxirank.backend.models.TripFeedback;
import com.taxirank.backend.repositories.TripFeedbackRepository;
import com.taxirank.backend.repositories.TripRepository;
import com.taxirank.backend.services.RouteInsightsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

// Computed in memory per route: fine while a route sees hundreds of trips a month.
// Move to a nightly aggregate table when routes see tens of thousands.
@Service
public class RouteInsightsServiceImpl implements RouteInsightsService {

    static final int WINDOW_DAYS = 28;
    // Fewer reports than this for an hour and we'd be showing one rider's bad day
    static final int MIN_SAMPLES = 3;
    // A "wait" longer than this means the rider forgot to tap; leave it out
    static final long MAX_WAIT_MINUTES = 180;
    static final int FARE_REPORT_WINDOW_DAYS = 7;
    static final int MIN_FARE_REPORTS = 3;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private TripFeedbackRepository tripFeedbackRepository;

    @Override
    @Transactional(readOnly = true)
    public RouteInsights insights(Terminal terminal, DayType dayType, LocalDateTime now) {
        List<Trip> trips = tripRepository
                .findByTerminalIdAndBoardedAtIsNotNullAndDepartedAtAfter(terminal.getId(), now.minusDays(WINDOW_DAYS)).stream()
                .filter(t -> DayType.of(t.getDepartedAt().toLocalDate()) == dayType)
                .toList();

        Map<Integer, List<Long>> waitsByHour = trips.stream()
                .map(t -> Map.entry(t.getBoardedAt().getHour(), Duration.between(t.getBoardedAt(), t.getDepartedAt()).toMinutes()))
                .filter(e -> e.getValue() >= 0 && e.getValue() <= MAX_WAIT_MINUTES)
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

        List<HourlyWait> hours = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            List<Long> waits = waitsByHour.getOrDefault(hour, List.of());
            hours.add(new HourlyWait(hour, waits.size() >= MIN_SAMPLES ? median(waits) : null, waits.size()));
        }

        LocalTime latestSeen = trips.stream()
                .map(t -> t.getDepartedAt().toLocalTime().withSecond(0).withNano(0))
                .max(Comparator.naturalOrder())
                .orElse(null);
        Integer waitNow = dayType == DayType.of(now.toLocalDate()) ? hours.get(now.getHour()).typicalWaitMinutes() : null;

        return new RouteInsights(terminal.getId(), dayType, WINDOW_DAYS, trips.size(), hours, waitNow,
                terminal.lastTaxiOn(dayTypeDay(dayType)), latestSeen);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FareReport> fareReport(Terminal terminal, LocalDateTime now) {
        LocalDateTime since = now.minusDays(FARE_REPORT_WINDOW_DAYS);
        if (terminal.getFareConfirmedAt() != null && terminal.getFareConfirmedAt().isAfter(since)) {
            since = terminal.getFareConfirmedAt();
        }
        BigDecimal listed = BigDecimal.valueOf(terminal.getFare());
        return tripFeedbackRepository.findByTerminalIdAndCreatedAtAfterAndFarePaidIsNotNull(terminal.getId(), since).stream()
                .map(TripFeedback::getFarePaid)
                .map(BigDecimal::stripTrailingZeros)
                .filter(paid -> paid.compareTo(listed) != 0)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .filter(e -> e.getValue() >= MIN_FARE_REPORTS)
                .max(Map.Entry.comparingByValue())
                .map(e -> new FareReport(e.getKey(), e.getValue().intValue()));
    }

    private static int median(List<Long> values) {
        List<Long> sorted = values.stream().sorted().toList();
        int mid = sorted.size() / 2;
        return (int) (sorted.size() % 2 == 1 ? sorted.get(mid) : Math.round((sorted.get(mid - 1) + sorted.get(mid)) / 2.0));
    }

    private static java.time.DayOfWeek dayTypeDay(DayType dayType) {
        return switch (dayType) {
            case SATURDAY -> java.time.DayOfWeek.SATURDAY;
            case SUNDAY -> java.time.DayOfWeek.SUNDAY;
            default -> java.time.DayOfWeek.MONDAY;
        };
    }
}
