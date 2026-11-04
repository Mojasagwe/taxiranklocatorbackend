package com.taxirank.backend.enums;

import java.time.DayOfWeek;
import java.time.LocalDate;

/** Taxis run differently on weekdays, Saturdays and Sundays, so trends are kept per day type */
public enum DayType {
    WEEKDAY,
    SATURDAY,
    SUNDAY;

    public static DayType of(DayOfWeek day) {
        return switch (day) {
            case SATURDAY -> SATURDAY;
            case SUNDAY -> SUNDAY;
            default -> WEEKDAY;
        };
    }

    public static DayType of(LocalDate date) {
        return of(date.getDayOfWeek());
    }
}
