package com.taxirank.backend.services;

import com.taxirank.backend.dto.RouteDtos.FareReport;
import com.taxirank.backend.dto.RouteDtos.RouteInsights;
import com.taxirank.backend.enums.DayType;
import com.taxirank.backend.models.Terminal;

import java.time.LocalDateTime;
import java.util.Optional;

/** Trends worked out from riders' trip taps and after-trip answers */
public interface RouteInsightsService {
    RouteInsights insights(Terminal terminal, DayType dayType, LocalDateTime now);

    /** The most-reported different fare since the rank last confirmed its fare, if enough riders agree */
    Optional<FareReport> fareReport(Terminal terminal, LocalDateTime now);
}
