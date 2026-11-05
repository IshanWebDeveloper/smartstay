package com.smartstay.model;

/**
 * The status an administrator sets on a room.
 * "Reserved" is not stored here; it depends on the dates and comes from reservations.
 */
public enum RoomStatus {

    /** Can be booked. */
    AVAILABLE("Available"),

    /** Out of service. */
    UNDER_MAINTENANCE("Under Maintenance");

    private final String displayName;

    RoomStatus(String displayName) {
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
