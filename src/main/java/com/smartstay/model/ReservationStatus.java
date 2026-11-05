package com.smartstay.model;

/**
 * The state of a {@link Reservation}.
 */
public enum ReservationStatus {

    /** Holds the room for its dates. */
    CONFIRMED("Confirmed"),

    /** Cancelled; the room is free again. */
    CANCELLED("Cancelled");

    private final String displayName;

    ReservationStatus(String displayName) {
        this.displayName = displayName;
    }

    /** @return the name shown to users */
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
