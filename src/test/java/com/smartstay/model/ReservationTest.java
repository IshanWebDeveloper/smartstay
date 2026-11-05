package com.smartstay.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Checks {@link Reservation}.
 */
class ReservationTest {

    private static final LocalDate CHECK_IN = LocalDate.of(2026, 12, 10);

    private static Reservation suiteBooking() {
        Guest sarah = new Guest("P4826153", "Sarah Johnson", "0700000000", "sarah@example.com");
        Room r301 = new Room("R301", RoomType.SUITE, BedType.KING, RoomStatus.AVAILABLE);
        return new Reservation("SS-0002", sarah, r301, new DateRange(CHECK_IN, CHECK_IN.plusDays(3)), 2);
    }

    private static DateRange days(int fromOffset, int toOffset) {
        return new DateRange(CHECK_IN.plusDays(fromOffset), CHECK_IN.plusDays(toOffset));
    }

    @Test
    @DisplayName("A new booking is confirmed with the LKR 10,000 advance")
    void newBookingIsConfirmedWithAdvance() {
        Reservation booking = suiteBooking();
        assertEquals(ReservationStatus.CONFIRMED, booking.getStatus());
        assertTrue(booking.isActive());
        assertEquals(new BigDecimal("10000.00"), booking.getAdvancePaid());
        assertEquals(3, booking.getNights());
    }

    @Test
    @DisplayName("An active booking blocks overlapping dates but not back-to-back ones")
    void blocksOverlappingDates() {
        Reservation booking = suiteBooking();
        assertTrue(booking.blocksRoomDuring(days(0, 2)));
        assertFalse(booking.blocksRoomDuring(days(3, 5)));
    }

    @Test
    @DisplayName("A cancelled booking no longer blocks the room")
    void cancelledBookingFreesRoom() {
        Reservation booking = suiteBooking();
        booking.cancel();
        assertEquals(ReservationStatus.CANCELLED, booking.getStatus());
        assertFalse(booking.blocksRoomDuring(days(0, 2)));
    }

    @Test
    @DisplayName("Cancelling twice is rejected")
    void cancelTwiceRejected() {
        Reservation booking = suiteBooking();
        booking.cancel();
        assertThrows(IllegalStateException.class, booking::cancel);
    }

    @Test
    @DisplayName("Zero guests is rejected")
    void zeroGuestsRejected() {
        Reservation valid = suiteBooking();
        assertThrows(IllegalArgumentException.class, () -> new Reservation(
                "SS-0003", valid.getGuest(), valid.getRoom(), valid.getStay(), 0));
    }
}
