package com.smartstay.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.smartstay.exception.BookingException;
import com.smartstay.exception.ValidationException;
import com.smartstay.model.BedType;
import com.smartstay.model.DateRange;
import com.smartstay.model.Guest;
import com.smartstay.model.Reservation;
import com.smartstay.model.Room;
import com.smartstay.model.RoomStatus;
import com.smartstay.model.RoomType;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Checks {@link BookingRules} with a fixed clock.
 */
class BookingRulesTest {

    private static final ZoneId COLOMBO = ZoneId.of("Asia/Colombo");
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 5);
    private static final Guest GUEST =
            new Guest("199012345678", "Nimal Perera", "0771234567", "nimal@example.com");

    private static BookingRules rulesAt(LocalDateTime now) {
        return new BookingRules(Clock.fixed(now.atZone(COLOMBO).toInstant(), COLOMBO));
    }

    private static BookingRules rulesAtNoonToday() {
        return rulesAt(TODAY.atTime(12, 0));
    }

    private static DateRange stay(int checkInOffset, int nights) {
        LocalDate checkIn = TODAY.plusDays(checkInOffset);
        return new DateRange(checkIn, checkIn.plusDays(nights));
    }

    private static Room room(RoomStatus status) {
        return new Room("R301", RoomType.SUITE, BedType.KING, status);
    }

    private static Reservation booking(Room room, DateRange stay) {
        return new Reservation("SS-0002", GUEST, room, stay, 2);
    }

    // Lead time

    @Test
    @DisplayName("Check-in tomorrow is rejected")
    void checkInTomorrowRejected() {
        assertThrows(BookingException.class, () -> rulesAtNoonToday().checkLeadTime(stay(1, 2)));
    }

    @Test
    @DisplayName("Check-in in 2 days is accepted")
    void checkInInTwoDaysAccepted() {
        assertDoesNotThrow(() -> rulesAtNoonToday().checkLeadTime(stay(2, 2)));
    }

    @Test
    @DisplayName("Lead time is by calendar day, even just before midnight")
    void leadTimeUsesCalendarDays() {
        BookingRules lateTonight = rulesAt(TODAY.atTime(23, 59));
        assertEquals(TODAY.plusDays(2), lateTonight.earliestCheckIn());
        assertDoesNotThrow(() -> lateTonight.checkLeadTime(stay(2, 1)));
    }

    // Cancellation window

    @Test
    @DisplayName("Cut-off is 14:00 the day before check-in")
    void cutoffIsDayBeforeAtCheckInTime() {
        assertEquals(LocalDateTime.of(2026, 11, 9, 14, 0),
                rulesAtNoonToday().cancellationCutoff(LocalDate.of(2026, 11, 10)));
    }

    @Test
    @DisplayName("Cancelling 1 minute before the cut-off is allowed")
    void oneMinuteBeforeCutoffAllowed() {
        LocalDate checkIn = TODAY.plusDays(1);
        BookingRules rules = rulesAt(TODAY.atTime(13, 59));
        assertDoesNotThrow(() -> rules.checkCancellationWindow(checkIn));
    }

    @Test
    @DisplayName("Cancelling at the cut-off is rejected")
    void atCutoffRejected() {
        LocalDate checkIn = TODAY.plusDays(1);
        BookingRules rules = rulesAt(TODAY.atTime(14, 0));
        assertThrows(BookingException.class, () -> rules.checkCancellationWindow(checkIn));
    }

    @Test
    @DisplayName("A booking for tomorrow cannot be cancelled after the cut-off")
    void tomorrowsBookingRejectedAfterCutoff() {
        Reservation tomorrow = booking(room(RoomStatus.AVAILABLE), stay(1, 2));
        BookingRules rules = rulesAt(TODAY.atTime(15, 0));
        assertThrows(BookingException.class, () -> rules.checkCancellable(tomorrow));
    }

    @Test
    @DisplayName("A booking well ahead can be cancelled, but only once")
    void cancelOnlyOnce() {
        Reservation future = booking(room(RoomStatus.AVAILABLE), stay(10, 3));
        BookingRules rules = rulesAtNoonToday();
        assertDoesNotThrow(() -> rules.checkCancellable(future));

        future.cancel();
        BookingException error = assertThrows(BookingException.class, () -> rules.checkCancellable(future));
        assertEquals("Reservation SS-0002 is already cancelled.", error.getMessage());
    }

    // Occupancy

    @Test
    @DisplayName("Up to capacity plus 2 guests are allowed")
    void maxOccupancyAllowed() {
        assertDoesNotThrow(() -> rulesAtNoonToday().checkOccupancy(RoomType.DELUXE, 4));
    }

    @Test
    @DisplayName("More than capacity plus 2 guests are rejected")
    void overMaxOccupancyRejected() {
        assertThrows(BookingException.class, () -> rulesAtNoonToday().checkOccupancy(RoomType.DELUXE, 5));
    }

    @Test
    @DisplayName("Zero guests is rejected")
    void zeroGuestsRejected() {
        assertThrows(ValidationException.class, () -> rulesAtNoonToday().checkOccupancy(RoomType.STANDARD, 0));
    }

    // Room availability

    @Test
    @DisplayName("A room under maintenance cannot be booked")
    void maintenanceRoomRejected() {
        Room room = room(RoomStatus.UNDER_MAINTENANCE);
        assertThrows(BookingException.class,
                () -> rulesAtNoonToday().checkRoomBookable(room, stay(5, 2), List.of()));
    }

    @Test
    @DisplayName("A room with an overlapping booking cannot be booked")
    void overlappingBookingRejected() {
        Room room = room(RoomStatus.AVAILABLE);
        List<Reservation> existing = List.of(booking(room, stay(10, 3)));
        assertThrows(BookingException.class,
                () -> rulesAtNoonToday().checkRoomBookable(room, stay(11, 1), existing));
    }

    @Test
    @DisplayName("Back-to-back and cancelled bookings do not block the room")
    void backToBackAndCancelledAllowed() {
        Room room = room(RoomStatus.AVAILABLE);
        Reservation cancelled = booking(room, stay(20, 3));
        cancelled.cancel();
        List<Reservation> existing = List.of(booking(room, stay(10, 3)), cancelled);
        BookingRules rules = rulesAtNoonToday();

        assertDoesNotThrow(() -> rules.checkRoomBookable(room, stay(13, 2), existing));
        assertDoesNotThrow(() -> rules.checkRoomBookable(room, stay(20, 3), existing));
    }
}
