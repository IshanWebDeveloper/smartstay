package com.smartstay.model;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;

/**
 * A hotel room. Its rate comes from its {@link RoomType}; the ID never changes.
 */
public final class Room {

    private final String roomId;
    private RoomType type;
    private BedType bedType;
    private RoomStatus status;

    /**
     * Creates a room.
     *
     * @param roomId  the unique ID, for example "R101", stored in upper case
     * @param type    the room type
     * @param bedType the bed type
     * @param status  the status
     */
    public Room(String roomId, RoomType type, BedType bedType, RoomStatus status) {
        Objects.requireNonNull(roomId, "Room ID is required.");
        if (roomId.isBlank()) {
            throw new IllegalArgumentException("Room ID must not be blank.");
        }
        this.roomId = roomId.trim().toUpperCase(Locale.ROOT);
        this.type = Objects.requireNonNull(type, "Room type is required.");
        this.bedType = Objects.requireNonNull(bedType, "Bed type is required.");
        this.status = Objects.requireNonNull(status, "Room status is required.");
    }

    /** @return the room ID */
    public String getRoomId() {
        return roomId;
    }

    /** @return the room type */
    public RoomType getType() {
        return type;
    }

    /** @return the bed type */
    public BedType getBedType() {
        return bedType;
    }

    /** @return the status */
    public RoomStatus getStatus() {
        return status;
    }

    /** @return the nightly rate of the room type, in LKR */
    public BigDecimal getDailyRate() {
        return type.getDailyRate();
    }

    /** @return whether the room is out of service */
    public boolean isUnderMaintenance() {
        return status == RoomStatus.UNDER_MAINTENANCE;
    }

    /** @param newType the new room type */
    public void changeType(RoomType newType) {
        this.type = Objects.requireNonNull(newType, "Room type is required.");
    }

    /** @param newBedType the new bed type */
    public void changeBedType(BedType newBedType) {
        this.bedType = Objects.requireNonNull(newBedType, "Bed type is required.");
    }

    /** @param newStatus the new status */
    public void changeStatus(RoomStatus newStatus) {
        this.status = Objects.requireNonNull(newStatus, "Room status is required.");
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof Room room && roomId.equals(room.roomId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomId);
    }

    @Override
    public String toString() {
        return roomId + " (" + type + ", " + bedType + ", " + status + ")";
    }
}
