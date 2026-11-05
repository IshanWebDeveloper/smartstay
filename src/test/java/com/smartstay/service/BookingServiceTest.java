package com.smartstay.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.smartstay.exception.BookingException;
import com.smartstay.exception.NotFoundException;
import com.smartstay.exception.ValidationException;
import com.smartstay.model.BedType;
import com.smartstay.model.DateRange;
import com.smartstay.model.Reservation;
import com.smartstay.model.ReservationStatus;
import com.smartstay.model.Room;
import com.smartstay.model.RoomStatus;
import com.smartstay.model.RoomType;
import com.smartstay.repository.InMemoryGuestRepository;
import com.smartstay.repository.InMemoryReservationRepository;
import com.smartstay.repository.InMemoryRoomRepository;
import com.smartstay.util.BookingIdGenerator;
import com.smartstay.validation.BookingRules;
import com.smartstay.validation.GuestValidator;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Checks {@link BookingService} at a fixed date, with rooms and guests like the seed data.
 */
class BookingServiceTest {

    private static final ZoneId COLOMBO = ZoneId.of("Asia/Colombo");
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 5);
    private static final String NIMAL = "199012345678";
    private static final String SARAH = "P4826153";

    private BookingService service;
    private RoomService roomService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(TODAY.atTime(12, 0).atZone(COLOMBO).toInstant(), COLOMBO);
        InMemoryReservationRepository reservations = new InMemoryReservationRepository();
        roomService = new RoomService(new InMemoryRoomRepository(), reservations, clock);
        GuestService guestService = new GuestService(new InMemoryGuestRepository(), new GuestValidator());
        service = new BookingService(roomService, guestService, reservations,
                new BookingRules(clock), new BookingIdGenerator());

        roomService.addRoom("R101", RoomType.STANDARD, BedType.SINGLE);
        roomService.addRoom("R103", RoomType.STANDARD, BedType.DOUBLE);
        roomService.changeStatus("R103", RoomStatus.UNDER_MAINTENANCE);
        roomService.addRoom("R202", RoomType.DELUXE, BedType.KING);
        roomService.addRoom("R201", RoomType.DELUXE, BedType.QUEEN);
        roomService.addRoom("R301", RoomType.SUITE, BedType.KING);
        roomService.addRoom("R302", RoomType.SUITE, BedType.QUEEN);
        guestService.register(NIMAL, "Nimal Perera", "0771234567", "nimal@example.com");
        guestService.register(SARAH, "Sarah Johnson", "0700000000", "sarah@example.com");
    }

    private static DateRange days(int checkInOffset, int checkOutOffset) {
        return new DateRange(TODAY.plusDays(checkInOffset), TODAY.plusDays(checkOutOffset));
    }

    private Reservation bookSuiteForSarah() {
        return service.createReservation(SARAH, "R301", days(10, 13), 2);
    }

    private List<String> availableIds(RoomType type, DateRange stay) {
        return service.findAvailableRooms(type, stay).stream().map(Room::getRoomId).toList();
    }

    private static List<String> ids(List<Reservation> reservations) {
        return reservations.stream().map(Reservation::getBookingId).toList();
    }

    // Searching

    @Test
    @DisplayName("Search lists free rooms of the type, sorted by ID")
    void searchListsFreeRoomsSorted() {
        assertEquals(List.of("R201", "R202"), availableIds(RoomType.DELUXE, days(20, 22)));
    }

    @Test
    @DisplayName("Search hides a room with an overlapping booking")
    void searchHidesOverlapping() {
        bookSuiteForSarah();
        assertEquals(List.of("R302"), availableIds(RoomType.SUITE, days(10, 12)));
    }

    @Test
    @DisplayName("Search shows a room for a back-to-back stay")
    void searchShowsBackToBack() {
        bookSuiteForSarah();
        assertEquals(List.of("R301", "R302"), availableIds(RoomType.SUITE, days(13, 15)));
    }

    @Test
    @DisplayName("Search hides a room under maintenance")
    void searchHidesMaintenance() {
        assertEquals(List.of("R101"), availableIds(RoomType.STANDARD, days(20, 22)));
    }

    // Booking

    @Test
    @DisplayName("A valid booking is confirmed with the advance and a sequential ID")
    void createReservation() {
        Reservation first = service.createReservation(NIMAL, "r101", days(3, 5), 2);
        Reservation second = bookSuiteForSarah();

        assertEquals("SS-0001", first.getBookingId());
        assertEquals("SS-0002", second.getBookingId());
        assertEquals(ReservationStatus.CONFIRMED, first.getStatus());
        assertEquals(new BigDecimal("10000.00"), first.getAdvancePaid());
        assertEquals(first, service.findByBookingId("SS-0001"));
    }

    @Test
    @DisplayName("Check-in in 2 days is accepted")
    void leadTimeBoundaryAccepted() {
        assertEquals("SS-0001", service.createReservation(NIMAL, "R101", days(2, 3), 1).getBookingId());
    }

    @Test
    @DisplayName("Check-in tomorrow is rejected")
    void leadTimeRejected() {
        DateRange tomorrow = days(1, 2);
        assertThrows(BookingException.class, () -> service.createReservation(NIMAL, "R101", tomorrow, 1));
    }

    @Test
    @DisplayName("A room under maintenance cannot be booked")
    void maintenanceRejected() {
        DateRange stay = days(5, 6);
        assertThrows(BookingException.class, () -> service.createReservation(NIMAL, "R103", stay, 1));
    }

    @Test
    @DisplayName("An overlapping booking is rejected")
    void overlapRejected() {
        bookSuiteForSarah();
        DateRange overlap = days(11, 12);
        assertThrows(BookingException.class, () -> service.createReservation(NIMAL, "R301", overlap, 2));
    }

    @Test
    @DisplayName("Too many guests are rejected")
    void tooManyGuestsRejected() {
        DateRange stay = days(5, 6);
        assertThrows(BookingException.class, () -> service.createReservation(NIMAL, "R201", stay, 5));
    }

    @Test
    @DisplayName("Zero guests is rejected")
    void zeroGuestsRejected() {
        DateRange stay = days(5, 6);
        assertThrows(ValidationException.class, () -> service.createReservation(NIMAL, "R201", stay, 0));
    }

    @Test
    @DisplayName("An unregistered guest or unknown room is rejected")
    void unknownGuestOrRoomRejected() {
        DateRange stay = days(5, 6);
        assertThrows(NotFoundException.class, () -> service.createReservation("188888888V", "R101", stay, 1));
        assertThrows(NotFoundException.class, () -> service.createReservation(NIMAL, "R999", stay, 1));
    }

    @Test
    @DisplayName("A rejected booking does not use up a booking ID")
    void rejectedBookingKeepsSequence() {
        DateRange tomorrow = days(1, 2);
        assertThrows(BookingException.class, () -> service.createReservation(NIMAL, "R101", tomorrow, 1));

        assertEquals("SS-0001", bookSuiteForSarah().getBookingId());
    }

    // Finding reservations

    @Test
    @DisplayName("Finds by booking ID, ignoring case")
    void findByBookingId() {
        bookSuiteForSarah();
        assertEquals("SS-0001", service.findByBookingId(" ss-0001 ").getBookingId());
    }

    @Test
    @DisplayName("An unknown or blank booking ID is reported")
    void unknownBookingId() {
        assertThrows(NotFoundException.class, () -> service.findByBookingId("SS-9999"));
        assertThrows(ValidationException.class, () -> service.findByBookingId(" "));
    }

    @Test
    @DisplayName("Finds by part of the guest name, ignoring case")
    void findByGuestName() {
        service.createReservation(NIMAL, "R201", days(3, 5), 2);
        bookSuiteForSarah();

        assertEquals(List.of("SS-0001"), ids(service.findByGuestName("nimal")));
        assertTrue(service.findByGuestName("zzz").isEmpty());
        assertThrows(ValidationException.class, () -> service.findByGuestName("  "));
    }

    @Test
    @DisplayName("Finds by check-in date")
    void findByCheckInDate() {
        service.createReservation(NIMAL, "R201", days(3, 5), 2);

        assertEquals(List.of("SS-0001"), ids(service.findByCheckInDate(TODAY.plusDays(3))));
        assertTrue(service.findByCheckInDate(TODAY.plusDays(40)).isEmpty());
    }

    @Test
    @DisplayName("Lists all reservations in check-in date order")
    void listInDateOrder() {
        bookSuiteForSarah();
        service.createReservation(NIMAL, "R201", days(3, 5), 2);

        assertEquals(List.of("SS-0002", "SS-0001"), ids(service.listReservations()));
    }

    // Cancelling

    @Test
    @DisplayName("Cancelling frees the room for the same dates")
    void cancelFreesRoom() {
        bookSuiteForSarah();

        Reservation cancelled = service.cancelReservation("SS-0001");

        assertFalse(cancelled.isActive());
        assertEquals(List.of("R301", "R302"), availableIds(RoomType.SUITE, days(10, 12)));
    }

    @Test
    @DisplayName("Cancelling twice is rejected")
    void cancelTwiceRejected() {
        bookSuiteForSarah();
        service.cancelReservation("SS-0001");

        assertThrows(BookingException.class, () -> service.cancelReservation("SS-0001"));
    }

    @Test
    @DisplayName("Cancelling an unknown reservation is reported")
    void cancelUnknown() {
        assertThrows(NotFoundException.class, () -> service.cancelReservation("SS-9999"));
    }

    @Test
    @DisplayName("Cancelling after the cut-off is rejected")
    void cancelAfterCutoffRejected() {
        Clock later = Clock.fixed(TODAY.plusDays(9).atTime(14, 0).atZone(COLOMBO).toInstant(), COLOMBO);
        InMemoryReservationRepository reservations = new InMemoryReservationRepository();
        reservations.save(bookSuiteForSarah());
        BookingService lateService = new BookingService(roomService,
                new GuestService(new InMemoryGuestRepository(), new GuestValidator()),
                reservations, new BookingRules(later), new BookingIdGenerator());

        assertThrows(BookingException.class, () -> lateService.cancelReservation("SS-0001"));
    }
}
