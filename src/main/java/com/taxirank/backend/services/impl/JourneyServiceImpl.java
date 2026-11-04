package com.taxirank.backend.services.impl;

import com.taxirank.backend.dto.RouteDtos.ConnectingOption;
import com.taxirank.backend.dto.RouteDtos.DirectOption;
import com.taxirank.backend.dto.RouteDtos.JourneyResults;
import com.taxirank.backend.dto.RouteDtos.Place;
import com.taxirank.backend.dto.RouteDtos.RankPoint;
import com.taxirank.backend.dto.RouteDtos.RouteInsights;
import com.taxirank.backend.dto.RouteDtos.RouteLeg;
import com.taxirank.backend.enums.DayType;
import com.taxirank.backend.models.TaxiRank;
import com.taxirank.backend.models.Terminal;
import com.taxirank.backend.repositories.TaxiRankRepository;
import com.taxirank.backend.repositories.TerminalRepository;
import com.taxirank.backend.services.JourneyService;
import com.taxirank.backend.services.RouteInsightsService;
import com.taxirank.backend.utils.GeoDistance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

// Loads active ranks and routes into memory per search: fine for hundreds of ranks.
// Move to PostGIS distance queries when Travy covers thousands.
@Service
public class JourneyServiceImpl implements JourneyService {

    // A route "goes to" a destination when it ends this close to it
    static final double DESTINATION_MATCH_KM = 1.5;
    static final double MIN_RADIUS_KM = 0.5;
    static final double MAX_RADIUS_KM = 10;
    // Outside the radius, how far we look for a direct taxi when there's none inside it
    static final double FURTHER_KM = 15;
    static final int MAX_CONNECTING = 5;
    static final int MAX_FURTHER = 3;
    // Walking ~4.8 km/h, and streets are rarely straight: about 1.3× the straight-line distance
    static final double WALK_KM_PER_MIN = 4.8 / 60;
    static final double STREET_FACTOR = 1.3;
    // Time to walk between bays and catch the next taxi when changing ranks, used for "leave by"
    static final int CHANGE_BUFFER_MINUTES = 15;

    private static final Pattern HOURS = Pattern.compile("(\\d+)\\s*h");
    private static final Pattern MINUTES = Pattern.compile("(\\d+)\\s*m");

    @Autowired
    private TaxiRankRepository taxiRankRepository;

    @Autowired
    private TerminalRepository terminalRepository;

    @Autowired
    private RouteInsightsService insightsService;

    @Override
    @Transactional(readOnly = true)
    public JourneyResults search(double fromLat, double fromLng, double toLat, double toLng, double radiusKm, LocalDateTime now) {
        requireCoordinates(fromLat, fromLng);
        requireCoordinates(toLat, toLng);
        double radius = Math.max(MIN_RADIUS_KM, Math.min(MAX_RADIUS_KM, radiusKm));

        List<TaxiRank> ranks = taxiRankRepository.findAll().stream()
                .filter(r -> !Boolean.FALSE.equals(r.getIsActive()) && r.getLatitude() != null && r.getLongitude() != null)
                .toList();
        Map<Long, List<Terminal>> routesByRank = new HashMap<>();
        terminalRepository.findAll().stream()
                .filter(Terminal::isActive)
                .forEach(t -> routesByRank.computeIfAbsent(t.getTaxiRank().getId(), k -> new ArrayList<>()).add(t));

        Map<Long, RouteLeg> legs = new HashMap<>();
        Search search = new Search(fromLat, fromLng, toLat, toLng, now, routesByRank,
                t -> legs.computeIfAbsent(t.getId(), id -> leg(t, now)));

        List<TaxiRank> inRadius = ranks.stream().filter(r -> search.walkKm(r) <= radius).toList();

        List<DirectOption> direct = inRadius.stream()
                .flatMap(r -> search.directFrom(r).map(t -> search.direct(r, t)))
                .sorted(Comparator.comparingDouble(DirectOption::walkKm).thenComparing(o -> o.route().fare()))
                .toList();

        List<ConnectingOption> connecting = inRadius.stream()
                .flatMap(search::connectingFrom)
                .sorted(Comparator.comparing(ConnectingOption::totalFare).thenComparingDouble(ConnectingOption::walkKm))
                .limit(MAX_CONNECTING)
                .toList();

        List<DirectOption> further = !direct.isEmpty() ? List.of() : ranks.stream()
                .filter(r -> search.walkKm(r) > radius && search.walkKm(r) <= FURTHER_KM)
                .flatMap(r -> search.directFrom(r).map(t -> search.direct(r, t)))
                .sorted(Comparator.comparingDouble(DirectOption::walkKm))
                .limit(MAX_FURTHER)
                .toList();

        return new JourneyResults(radius, inRadius.size(), direct, connecting, further);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Place> places(String query) {
        if (!StringUtils.hasText(query) || query.trim().length() < 2) {
            return List.of();
        }
        String q = query.trim().toLowerCase(Locale.ROOT);
        Map<String, Place> found = new LinkedHashMap<>();
        terminalRepository.findAll().stream()
                .filter(Terminal::isActive)
                .filter(t -> t.getName().toLowerCase(Locale.ROOT).contains(q))
                .forEach(t -> {
                    double[] point = t.destinationPoint();
                    if (point != null) {
                        String area = t.getEndsAtRank() != null ? "Taxis end at " + t.getEndsAtRank().getName() : "Route destination";
                        found.putIfAbsent(t.getName().toLowerCase(Locale.ROOT), new Place(t.getName(), area, point[0], point[1], "ROUTE_END"));
                    }
                });
        taxiRankRepository.findAll().stream()
                .filter(r -> r.getLatitude() != null && r.getLongitude() != null)
                .filter(r -> Stream.of(r.getName(), r.getCity(), r.getAddress()).filter(Objects::nonNull)
                        .anyMatch(v -> v.toLowerCase(Locale.ROOT).contains(q)))
                .forEach(r -> found.putIfAbsent(r.getName().toLowerCase(Locale.ROOT),
                        new Place(r.getName(), Stream.of(r.getAddress(), r.getCity()).filter(Objects::nonNull)
                                .reduce((a, b) -> a + " · " + b).orElse(""), r.getLatitude(), r.getLongitude(), "RANK")));
        return found.values().stream().limit(10).toList();
    }

    private RouteLeg leg(Terminal t, LocalDateTime now) {
        RouteInsights insights = insightsService.insights(t, DayType.of(now.toLocalDate()), now);
        return new RouteLeg(t.getId(), t.getName(), t.getFare(), t.getBay(), t.getTravelTime(), parseMinutes(t.getTravelTime()),
                t.getIntervalMinutes(), t.isRunningOn(now.toLocalDate()), t.getFirstTaxi(),
                t.lastTaxiOn(now.getDayOfWeek()), insights.typicalWaitNow(), insights.latestDepartureSeen(), insights.riderTrips());
    }

    /** "40 min", "1 h 10 min", "1h10m" → minutes; null if the rank wrote something else */
    static Integer parseMinutes(String travelTime) {
        if (travelTime == null) {
            return null;
        }
        String text = travelTime.toLowerCase(Locale.ROOT);
        Matcher h = HOURS.matcher(text);
        Matcher m = MINUTES.matcher(text.replaceAll("\\d+\\s*h(ours?|rs?)?", ""));
        Integer hours = h.find() ? Integer.valueOf(h.group(1)) : null;
        Integer minutes = m.find() ? Integer.valueOf(m.group(1)) : null;
        if (hours == null && minutes == null) {
            return null;
        }
        return (hours == null ? 0 : hours * 60) + (minutes == null ? 0 : minutes);
    }

    private static void requireCoordinates(double lat, double lng) {
        if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            throw new IllegalArgumentException("Invalid coordinates");
        }
    }

    /** One search's inputs and the helpers that need them */
    private record Search(double fromLat, double fromLng, double toLat, double toLng, LocalDateTime now,
            Map<Long, List<Terminal>> routesByRank, java.util.function.Function<Terminal, RouteLeg> legOf) {

        double walkKm(TaxiRank rank) {
            return GeoDistance.km(fromLat, fromLng, rank.getLatitude(), rank.getLongitude());
        }

        int walkMinutes(double km) {
            return (int) Math.ceil(km * STREET_FACTOR / WALK_KM_PER_MIN);
        }

        boolean endsNearDestination(Terminal t) {
            double[] point = t.destinationPoint();
            return point != null && GeoDistance.km(point[0], point[1], toLat, toLng) <= DESTINATION_MATCH_KM;
        }

        Stream<Terminal> directFrom(TaxiRank rank) {
            return routesByRank.getOrDefault(rank.getId(), List.of()).stream().filter(this::endsNearDestination);
        }

        DirectOption direct(TaxiRank rank, Terminal t) {
            double km = round(walkKm(rank));
            return new DirectOption(RankPoint.from(rank), km, walkMinutes(km), legOf.apply(t));
        }

        /** Routes from this rank that end at another rank, which has a route to the destination; skipped if this rank goes direct */
        Stream<ConnectingOption> connectingFrom(TaxiRank rank) {
            if (directFrom(rank).findAny().isPresent()) {
                return Stream.empty();
            }
            double km = round(walkKm(rank));
            return routesByRank.getOrDefault(rank.getId(), List.of()).stream()
                    .filter(first -> first.getEndsAtRank() != null && !first.getEndsAtRank().getId().equals(rank.getId())
                            && !Boolean.FALSE.equals(first.getEndsAtRank().getIsActive()))
                    .flatMap(first -> routesByRank.getOrDefault(first.getEndsAtRank().getId(), List.of()).stream()
                            .filter(this::endsNearDestination)
                            .map(second -> connecting(rank, km, first, second)));
        }

        ConnectingOption connecting(TaxiRank rank, double km, Terminal first, Terminal second) {
            RouteLeg leg1 = legOf.apply(first);
            RouteLeg leg2 = legOf.apply(second);
            int walk = walkMinutes(km);
            Integer total = leg1.travelMinutes() == null || leg2.travelMinutes() == null ? null
                    : walk + waitOrZero(leg1) + leg1.travelMinutes() + CHANGE_BUFFER_MINUTES + leg2.travelMinutes();
            LocalTime leaveBy = leg2.lastTaxiToday() == null || leg1.travelMinutes() == null ? null
                    : leg2.lastTaxiToday().minusMinutes(leg1.travelMinutes() + waitOrZero(leg1) + CHANGE_BUFFER_MINUTES + walk);
            return new ConnectingOption(RankPoint.from(rank), km, walk, leg1, RankPoint.from(first.getEndsAtRank()), leg2,
                    leg1.fare() + leg2.fare(), total, leaveBy);
        }

        private static int waitOrZero(RouteLeg leg) {
            return leg.typicalWaitNow() != null ? leg.typicalWaitNow() : leg.intervalMinutes() != null ? leg.intervalMinutes() : 0;
        }

        private static double round(double km) {
            return Math.round(km * 100) / 100.0;
        }
    }
}
