package com.taxirank.backend.dto;

import com.taxirank.backend.enums.DayType;
import com.taxirank.backend.models.TaxiRank;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

/** What riders see about routes: trends, journey options and places. Rank admins' editing uses TerminalDTO. */
public final class RouteDtos {
    private RouteDtos() {
    }

    /** Typical minutes from "in the taxi" to "taxi left" for one hour; null when too few riders reported */
    public record HourlyWait(int hour, Integer typicalWaitMinutes, int samples) {
    }

    /** riderTrips: trips with both taps in the window the trends cover */
    public record RouteInsights(Long terminalId, DayType dayType, int windowDays, int riderTrips,
            List<HourlyWait> hours, Integer typicalWaitNow, LocalTime lastTaxiByRank, LocalTime latestDepartureSeen) {
    }

    /** Several riders paid the same different fare since the rank last confirmed it */
    public record FareReport(BigDecimal reportedFare, int reports) {
    }

    public record RankPoint(Long id, String code, String name, String address, String city, Double latitude, Double longitude) {
        public static RankPoint from(TaxiRank rank) {
            return new RankPoint(rank.getId(), rank.getCode(), rank.getName(), rank.getAddress(), rank.getCity(),
                    rank.getLatitude(), rank.getLongitude());
        }
    }

    /** One route as a rider needs it right now */
    public record RouteLeg(Long terminalId, String destination, Double fare, String bay, String travelTime,
            Integer travelMinutes, Integer intervalMinutes, boolean runningToday, LocalTime firstTaxi,
            LocalTime lastTaxiToday, Integer typicalWaitNow, LocalTime latestDepartureSeen, int riderTrips) {
    }

    public record DirectOption(RankPoint rank, double walkKm, int walkMinutes, RouteLeg route) {
    }

    /** Walk to rank, taxi to the change rank, second taxi to the destination */
    public record ConnectingOption(RankPoint rank, double walkKm, int walkMinutes, RouteLeg firstLeg,
            RankPoint changeAt, RouteLeg secondLeg, double totalFare, Integer totalMinutes, LocalTime leaveBy) {
    }

    /** further: direct options outside the radius, offered only when nothing direct is inside it */
    public record JourneyResults(double radiusKm, int ranksInRadius, List<DirectOption> direct,
            List<ConnectingOption> connecting, List<DirectOption> further) {
    }

    /** A destination riders can search for before Google place search is on: route ends and ranks */
    public record Place(String name, String area, double latitude, double longitude, String kind) {
    }
}
