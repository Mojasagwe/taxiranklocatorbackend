package com.taxirank.backend.enums;

/** Whether a rider plans a trip, is in the taxi now, or has taken it */
public enum TripStatus {
    PLANNED,
    /** In the taxi: started with "I'm in the taxi", ends with "I've arrived" */
    ACTIVE,
    TAKEN
}
