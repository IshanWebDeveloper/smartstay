package com.smartstay.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Checks {@link Room}.
 */
class RoomTest {

    @Test
    @DisplayName("Rate is read from the room type, and follows a type change")
    void rateFollowsType() {
        Room room = new Room("R501", RoomType.DELUXE, BedType.KING, RoomStatus.AVAILABLE);
        assertEquals(new BigDecimal("9000.00"), room.getDailyRate());

        room.changeType(RoomType.SUITE);
        assertEquals(new BigDecimal("14000.00"), room.getDailyRate());
    }

    @Test
    @DisplayName("Bed and status can change; the ID cannot")
    void changeBedAndStatus() {
        Room room = new Room("r102", RoomType.STANDARD, BedType.DOUBLE, RoomStatus.AVAILABLE);
        room.changeBedType(BedType.QUEEN);
        room.changeStatus(RoomStatus.UNDER_MAINTENANCE);

        assertEquals("R102", room.getRoomId());
        assertEquals(BedType.QUEEN, room.getBedType());
        assertTrue(room.isUnderMaintenance());
    }

    @Test
    @DisplayName("Rooms are equal by ID")
    void equalityById() {
        Room first = new Room("R101", RoomType.STANDARD, BedType.SINGLE, RoomStatus.AVAILABLE);
        Room second = new Room("R101", RoomType.SUITE, BedType.KING, RoomStatus.UNDER_MAINTENANCE);
        assertEquals(first, second);
        assertFalse(first.isUnderMaintenance());
    }

    @Test
    @DisplayName("Blank ID and missing values are rejected")
    void invalidRoomRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Room(" ", RoomType.STANDARD, BedType.SINGLE, RoomStatus.AVAILABLE));
        assertThrows(NullPointerException.class,
                () -> new Room("R101", null, BedType.SINGLE, RoomStatus.AVAILABLE));
    }
}
