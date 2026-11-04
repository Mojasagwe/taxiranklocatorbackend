package com.taxirank.backend.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.taxirank.backend.enums.DayType;
import com.taxirank.backend.enums.TripStatus;
import com.taxirank.backend.models.Profile;
import com.taxirank.backend.models.TaxiRank;
import com.taxirank.backend.models.Terminal;
import com.taxirank.backend.models.Trip;
import com.taxirank.backend.models.User;
import com.taxirank.backend.repositories.TerminalRepository;
import com.taxirank.backend.repositories.TripRepository;

/**
 * Finding a taxi: journey search (direct, one change, further), places, riders' trip taps,
 * the trends they feed, fare reports for the rank, and the rank admin's route fields.
 */
class FindTaxiIntegrationTest extends IntegrationTestSupport {

    // Rissik St, Johannesburg CBD: ~0.4 km from Central, ~1 km from Bree Street
    static final double HERE_LAT = -26.2041;
    static final double HERE_LNG = 28.0435;
    static final double BARA_LAT = -26.2610;
    static final double BARA_LNG = 27.9430;
    static final double MAMELODI_LAT = -25.7200;
    static final double MAMELODI_LNG = 28.3950;

    @Autowired
    TerminalRepository terminalRepository;

    @Autowired
    TripRepository tripRepository;

    private User rider;
    private TaxiRank central;
    private TaxiRank bree;
    private TaxiRank pretoria;
    private Terminal centralToBara;
    private Terminal breeToPretoria;
    private Terminal pretoriaToMamelodi;

    @BeforeEach
    void setUp() {
        rider = person("rider@test.local", "0821110001", Profile.COMMUTER);
        central = rank("CTR001", "Central Taxi Rank", -26.2041, 28.0473);
        bree = rank("BSR001", "Bree Street Taxi Rank", -26.2005, 28.0360);
        pretoria = rank("PCR001", "Pretoria Central Rank", -25.7479, 28.1879);

        centralToBara = route(central, "Soweto · Bara", 18.0, "40 min");
        centralToBara.setDestinationLatitude(BARA_LAT);
        centralToBara.setDestinationLongitude(BARA_LNG);
        centralToBara.setIntervalMinutes(15);
        centralToBara.setBay("Bay 4");
        terminalRepository.save(centralToBara);

        breeToPretoria = route(bree, "Pretoria", 45.0, "1 h 10 min");
        breeToPretoria.setEndsAtRank(pretoria);
        terminalRepository.save(breeToPretoria);

        pretoriaToMamelodi = route(pretoria, "Mamelodi West", 16.0, "30 min");
        pretoriaToMamelodi.setDestinationLatitude(MAMELODI_LAT);
        pretoriaToMamelodi.setDestinationLongitude(MAMELODI_LNG);
        pretoriaToMamelodi.setLastTaxiWeekday(LocalTime.of(19, 0));
        pretoriaToMamelodi.setLastTaxiSaturday(LocalTime.of(19, 0));
        pretoriaToMamelodi.setLastTaxiSunday(LocalTime.of(19, 0));
        terminalRepository.save(pretoriaToMamelodi);
    }

    // --- Journey search

    @Test
    void directRoutesWithinTheRadiusComeFirst() throws Exception {
        JsonNode results = data(mockMvc.perform(as(rider, journeys(BARA_LAT, BARA_LNG, 2))).andExpect(status().isOk()).andReturn());

        assertThat(results.get("ranksInRadius").asInt()).isEqualTo(2);
        assertThat(results.get("direct")).hasSize(1);
        JsonNode option = results.get("direct").get(0);
        assertThat(option.get("rank").get("name").asText()).isEqualTo("Central Taxi Rank");
        assertThat(option.get("walkMinutes").asInt()).isBetween(4, 10);
        assertThat(option.get("route").get("fare").asDouble()).isEqualTo(18.0);
        assertThat(option.get("route").get("bay").asText()).isEqualTo("Bay 4");
        assertThat(option.get("route").get("intervalMinutes").asInt()).isEqualTo(15);
        assertThat(option.get("route").get("runningToday").asBoolean()).isTrue();
        assertThat(results.get("connecting")).isEmpty();
        assertThat(results.get("further")).isEmpty();
    }

    @Test
    void withNoDirectTaxiItOffersOneChange() throws Exception {
        JsonNode results = data(mockMvc.perform(as(rider, journeys(MAMELODI_LAT, MAMELODI_LNG, 2))).andReturn());

        assertThat(results.get("direct")).isEmpty();
        assertThat(results.get("connecting")).hasSize(1);
        JsonNode option = results.get("connecting").get(0);
        assertThat(option.get("rank").get("name").asText()).isEqualTo("Bree Street Taxi Rank");
        assertThat(option.get("changeAt").get("name").asText()).isEqualTo("Pretoria Central Rank");
        assertThat(option.get("firstLeg").get("travelMinutes").asInt()).isEqualTo(70);
        assertThat(option.get("totalFare").asDouble()).isEqualTo(61.0);
        // Last Mamelodi taxi 19:00 minus 70 min ride, 15 min to change and the walk
        LocalTime leaveBy = LocalTime.parse(option.get("leaveBy").asText());
        assertThat(leaveBy).isBefore(LocalTime.of(17, 35)).isAfter(LocalTime.of(17, 0));
    }

    @Test
    void ranksOutsideTheRadiusAreOfferedOnlyWhenNothingIsInside() throws Exception {
        // ~3 km west of Central, 0.5 km radius: no rank inside
        JsonNode results = data(mockMvc.perform(as(rider, get("/api/journeys")
                .param("fromLat", "-26.2041").param("fromLng", "28.0170")
                .param("toLat", String.valueOf(BARA_LAT)).param("toLng", String.valueOf(BARA_LNG))
                .param("radiusKm", "0.5"))).andReturn());

        assertThat(results.get("ranksInRadius").asInt()).isZero();
        assertThat(results.get("direct")).isEmpty();
        assertThat(results.get("further").get(0).get("rank").get("name").asText()).isEqualTo("Central Taxi Rank");
    }

    @Test
    void pausedRoutesStillShowButSayNotRunning() throws Exception {
        User account = rankAccount(central);
        mockMvc.perform(as(account, put("/api/taxi-ranks/" + central.getId() + "/terminals/" + centralToBara.getId()),
                routeJson("Soweto · Bara", 18, "\"destinationLatitude\":" + BARA_LAT + ",\"destinationLongitude\":" + BARA_LNG
                        + ",\"runningToday\":false")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.runningToday").value(false));

        JsonNode results = data(mockMvc.perform(as(rider, journeys(BARA_LAT, BARA_LNG, 2))).andReturn());
        assertThat(results.get("direct").get(0).get("route").get("runningToday").asBoolean()).isFalse();
        assertThat(terminalRepository.findById(centralToBara.getId()).orElseThrow().getPausedOn()).isEqualTo(LocalDate.now());
    }

    @Test
    void searchNeedsALoginAndValidCoordinates() throws Exception {
        mockMvc.perform(journeys(BARA_LAT, BARA_LNG, 2)).andExpect(status().isForbidden());
        mockMvc.perform(as(rider, get("/api/journeys").param("fromLat", "100").param("fromLng", "28")
                .param("toLat", "-26").param("toLng", "28"))).andExpect(status().isBadRequest());
    }

    @Test
    void placesFindRouteEndsAndRanks() throws Exception {
        mockMvc.perform(as(rider, get("/api/places").param("q", "bara")))
                .andExpect(jsonPath("$.data[0].name").value("Soweto · Bara"))
                .andExpect(jsonPath("$.data[0].kind").value("ROUTE_END"))
                .andExpect(jsonPath("$.data[0].latitude").value(BARA_LAT));
        mockMvc.perform(as(rider, get("/api/places").param("q", "pretoria")))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].area").value("Taxis end at Pretoria Central Rank"))
                .andExpect(jsonPath("$.data[1].kind").value("RANK"));
        mockMvc.perform(as(rider, get("/api/places").param("q", "b"))).andExpect(jsonPath("$.data.length()").value(0));
    }

    // --- Riders' trip taps

    @Test
    void aTripGoesFromInTheTaxiToLeftToArrived() throws Exception {
        long tripId = data(mockMvc.perform(as(rider, post("/api/trips/start"), "{\"terminalId\":" + centralToBara.getId() + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.bay").value("Bay 4"))
                .andReturn()).get("id").asLong();

        mockMvc.perform(as(rider, post("/api/trips/start"), "{\"terminalId\":" + centralToBara.getId() + "}"))
                .andExpect(status().isConflict());
        mockMvc.perform(as(rider, get("/api/trips/active"))).andExpect(jsonPath("$.data.id").value(tripId));
        mockMvc.perform(as(rider, get("/api/trips"))).andExpect(jsonPath("$.data.length()").value(0));

        mockMvc.perform(as(rider, post("/api/trips/" + tripId + "/departed")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.departedAt").isNotEmpty());
        mockMvc.perform(as(rider, post("/api/trips/" + tripId + "/departed"))).andExpect(status().isBadRequest());

        mockMvc.perform(as(rider, post("/api/trips/" + tripId + "/arrived")))
                .andExpect(jsonPath("$.data.status").value("TAKEN"))
                .andExpect(jsonPath("$.data.arrivedAt").isNotEmpty());
        mockMvc.perform(as(rider, get("/api/trips/active"))).andExpect(jsonPath("$.data").doesNotExist());
        mockMvc.perform(as(rider, post("/api/trips/" + tripId + "/arrived"))).andExpect(status().isBadRequest());
    }

    @Test
    void aForgottenTripClosesItselfAfterSixHours() throws Exception {
        Trip old = trip(rider, centralToBara, LocalDateTime.now().minusHours(7), null);
        old.setStatus(TripStatus.ACTIVE);
        tripRepository.save(old);

        mockMvc.perform(as(rider, get("/api/trips/active"))).andExpect(jsonPath("$.data").doesNotExist());
        assertThat(tripRepository.findById(old.getId()).orElseThrow().getStatus()).isEqualTo(TripStatus.TAKEN);
        mockMvc.perform(as(rider, post("/api/trips/start"), "{\"terminalId\":" + centralToBara.getId() + "}"))
                .andExpect(status().isCreated());
    }

    @Test
    void feedbackIsOncePerOwnFinishedTrip() throws Exception {
        Trip done = trip(rider, centralToBara, LocalDateTime.now().minusHours(1), LocalDateTime.now().minusMinutes(50));
        User other = person("other@test.local", "0821110002", Profile.COMMUTER);

        mockMvc.perform(as(other, post("/api/trips/" + done.getId() + "/feedback"), "{\"rating\":\"GOOD\"}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(as(rider, post("/api/trips/" + done.getId() + "/feedback"),
                "{\"farePaid\":20,\"queue\":\"LONG_QUEUE\",\"rating\":\"OK\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(as(rider, post("/api/trips/" + done.getId() + "/feedback"), "{\"rating\":\"GOOD\"}"))
                .andExpect(status().isConflict());
        mockMvc.perform(as(rider, post("/api/trips/" + done.getId() + "/feedback"), "{\"farePaid\":-5}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void tripTapsNeedTheTripsPermission() throws Exception {
        mockMvc.perform(as(rankAccount(central), post("/api/trips/start"), "{\"terminalId\":" + centralToBara.getId() + "}"))
                .andExpect(status().isForbidden());
        assertThat(tripRepository.count()).isZero();
    }

    // --- Trends from riders

    @Test
    void insightsShowTheTypicalWaitByHourFromRidersTaps() throws Exception {
        // Same weekday last week, so it's in the 4-week window and today's day type
        LocalDateTime lastWeek = LocalDate.now().minusDays(7).atTime(9, 0);
        for (int wait : new int[] {8, 12, 10}) {
            trip(rider, centralToBara, lastWeek.plusMinutes(wait), lastWeek.plusMinutes(wait * 2L));
        }
        trip(rider, centralToBara, lastWeek.withHour(14), lastWeek.withHour(14).plusMinutes(30));
        // A forgotten tap: 5 hours "waiting" is left out
        trip(rider, centralToBara, lastWeek.withHour(15), lastWeek.withHour(20).plusMinutes(10));

        JsonNode insights = data(mockMvc.perform(get("/api/ranks/" + central.getId() + "/destinations/" + centralToBara.getId()
                + "/insights").param("day", DayType.of(LocalDate.now()).name()))
                .andExpect(status().isOk()).andReturn());

        assertThat(insights.get("riderTrips").asInt()).isEqualTo(5);
        assertThat(insights.get("hours").get(9).get("typicalWaitMinutes").asInt()).isEqualTo(10);
        assertThat(insights.get("hours").get(9).get("samples").asInt()).isEqualTo(3);
        // One rider's trip isn't a trend
        assertThat(insights.get("hours").get(14).get("typicalWaitMinutes").isNull()).isTrue();
        assertThat(insights.get("latestDepartureSeen").asText()).startsWith("20:10");
    }

    @Test
    void insightsAreForActiveRoutesOfThatRankOnly() throws Exception {
        mockMvc.perform(get("/api/ranks/" + bree.getId() + "/destinations/" + centralToBara.getId() + "/insights"))
                .andExpect(status().isNotFound());
    }

    // --- Fare reports and the rank admin's route fields

    @Test
    void ridersReportingAnotherFareAskTheRankToConfirmIt() throws Exception {
        for (int i = 0; i < 3; i++) {
            Trip done = trip(rider, centralToBara, LocalDateTime.now().minusHours(2 + i), LocalDateTime.now().minusHours(2 + i).plusMinutes(5));
            mockMvc.perform(as(rider, post("/api/trips/" + done.getId() + "/feedback"), "{\"farePaid\":20}")).andExpect(status().isOk());
        }
        User account = rankAccount(central);

        mockMvc.perform(as(account, get("/api/taxi-ranks/" + central.getId() + "/terminals")))
                .andExpect(jsonPath("$.data[0].fareReport.reportedFare").value(20))
                .andExpect(jsonPath("$.data[0].fareReport.reports").value(3));
        // Riders don't see the reports
        mockMvc.perform(as(rider, get("/api/taxi-ranks/" + central.getId() + "/terminals")))
                .andExpect(jsonPath("$.data[0].fareReport").doesNotExist());
        // Another rank can't answer for this one
        mockMvc.perform(as(rankAccount(bree), post("/api/taxi-ranks/" + central.getId() + "/terminals/" + centralToBara.getId()
                + "/confirm-fare"), "{\"fare\":99}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(as(account, post("/api/taxi-ranks/" + central.getId() + "/terminals/" + centralToBara.getId()
                + "/confirm-fare"), "{\"fare\":20}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fare").value(20.0));
        mockMvc.perform(as(account, get("/api/taxi-ranks/" + central.getId() + "/terminals")))
                .andExpect(jsonPath("$.data[0].fareReport").doesNotExist());
    }

    @Test
    void rankAdminsSetWhereARouteGoesAndHowItRuns() throws Exception {
        User account = rankAccount(bree);
        String path = "/api/taxi-ranks/" + bree.getId() + "/terminals/" + breeToPretoria.getId();

        mockMvc.perform(as(account, put(path), routeJson("Pretoria", 45, "\"endsAtRankId\":" + bree.getId())))
                .andExpect(status().isBadRequest());
        mockMvc.perform(as(account, put(path), routeJson("Pretoria", 45, "\"destinationLatitude\":-25.7")))
                .andExpect(status().isBadRequest());
        mockMvc.perform(as(account, put(path), routeJson("Pretoria", 45, "\"intervalMinutes\":0")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(as(account, put(path), routeJson("Pretoria", 45, "\"endsAtRankId\":" + pretoria.getId()
                + ",\"bay\":\"Pretoria line\",\"intervalMinutes\":20,\"firstTaxi\":\"05:00\",\"lastTaxiWeekday\":\"20:15\"")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.endsAtRankName").value("Pretoria Central Rank"))
                .andExpect(jsonPath("$.data.bay").value("Pretoria line"))
                .andExpect(jsonPath("$.data.intervalMinutes").value(20))
                .andExpect(jsonPath("$.data.lastTaxiWeekday").value("20:15:00"));
    }

    // --- Helpers

    private MockHttpServletRequestBuilder journeys(double toLat, double toLng, double radiusKm) {
        return get("/api/journeys").param("fromLat", String.valueOf(HERE_LAT)).param("fromLng", String.valueOf(HERE_LNG))
                .param("toLat", String.valueOf(toLat)).param("toLng", String.valueOf(toLng))
                .param("radiusKm", String.valueOf(radiusKm));
    }

    private static String routeJson(String name, double fare, String extra) {
        return "{\"name\":\"" + name + "\",\"fare\":" + fare + ",\"travelTime\":\"40 min\",\"distance\":\"25 km\"," + extra + "}";
    }

    private Terminal route(TaxiRank rank, String name, double fare, String travelTime) {
        Terminal terminal = new Terminal();
        terminal.setName(name);
        terminal.setFare(fare);
        terminal.setTravelTime(travelTime);
        terminal.setDistance("25 km");
        terminal.setTaxiRank(rank);
        return terminalRepository.save(terminal);
    }

    private Trip trip(User user, Terminal terminal, LocalDateTime boarded, LocalDateTime departed) {
        Trip trip = new Trip();
        trip.setUser(user);
        trip.setRank(terminal.getTaxiRank());
        trip.setTerminal(terminal);
        trip.setDestination(terminal.getName());
        trip.setFare(BigDecimal.valueOf(terminal.getFare()));
        trip.setStatus(TripStatus.TAKEN);
        trip.setTripDate(boarded);
        trip.setBoardedAt(boarded);
        trip.setDepartedAt(departed);
        return tripRepository.save(trip);
    }
}
