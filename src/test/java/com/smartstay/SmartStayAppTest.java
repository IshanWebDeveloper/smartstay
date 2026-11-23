package com.smartstay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.smartstay.exception.ValidationException;
import com.smartstay.model.DateRange;
import com.smartstay.model.Reservation;
import com.smartstay.model.Room;
import com.smartstay.repository.InMemoryGuestRepository;
import com.smartstay.repository.InMemoryReservationRepository;
import com.smartstay.repository.InMemoryRoomRepository;
import com.smartstay.ui.InputReader;
import com.smartstay.ui.TablePrinter;
import com.smartstay.util.BookingIdGenerator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Checks the seed data and drives the whole application through scripted console input.
 */
class SmartStayAppTest {

    private static final ZoneId COLOMBO = ZoneId.of("Asia/Colombo");
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 5);
    private static final String GUEST_MENU = "1";
    private static final String ADMIN_MENU = "2";
    private static final String BACK = "0";

    /** Runs the application at 15:00 today with the given input lines, returning the output. */
    private static String run(String... lines) {
        return runAt(TODAY.atTime(15, 0), lines);
    }

    private static String runAt(LocalDateTime now, String... lines) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        TablePrinter printer = new TablePrinter(new PrintStream(output, true, Charset.defaultCharset()));
        byte[] script = (String.join(System.lineSeparator(), lines) + System.lineSeparator())
                .getBytes(Charset.defaultCharset());
        InputReader input = new InputReader(new ByteArrayInputStream(script), printer);
        Clock clock = Clock.fixed(now.atZone(COLOMBO).toInstant(), COLOMBO);

        SmartStayApp.createMainMenu(clock, input, printer).start();
        return output.toString(Charset.defaultCharset());
    }

    private static String day(int offset) {
        return TODAY.plusDays(offset).toString();
    }

    private static String[] asAdmin(String... lines) {
        List<String> script = new ArrayList<>(List.of(ADMIN_MENU, SmartStayApp.DEMO_ADMIN_PASSWORD));
        script.addAll(List.of(lines));
        script.addAll(List.of(BACK, BACK));
        return script.toArray(String[]::new);
    }

    private static String[] asGuest(String... lines) {
        List<String> script = new ArrayList<>(List.of(GUEST_MENU));
        script.addAll(List.of(lines));
        script.addAll(List.of(BACK, BACK));
        return script.toArray(String[]::new);
    }

    // Seed data

    @Test
    @DisplayName("Seed data has the required rooms, guests and reservations")
    void seedData() {
        InMemoryRoomRepository rooms = new InMemoryRoomRepository();
        InMemoryGuestRepository guests = new InMemoryGuestRepository();
        InMemoryReservationRepository reservations = new InMemoryReservationRepository();
        BookingIdGenerator idGenerator = new BookingIdGenerator();

        SmartStayApp.loadSeedData(rooms, guests, reservations, idGenerator, TODAY);

        List<String> seededRooms = rooms.findAll().stream()
                .sorted(Comparator.comparing(Room::getRoomId)).map(Room::toString).toList();
        assertEquals(List.of(
                "R101 (Standard, Single, Available)",
                "R102 (Standard, Double, Available)",
                "R103 (Standard, Double, Under Maintenance)",
                "R201 (Deluxe, Queen, Available)",
                "R202 (Deluxe, King, Available)",
                "R301 (Suite, King, Available)",
                "R302 (Suite, Queen, Available)",
                "R401 (Family Suite, King, Available)",
                "R402 (Family Suite, Queen, Available)"), seededRooms);

        assertEquals("Nimal Perera", guests.findById("199012345678").orElseThrow().getFullName());
        assertEquals("Sarah Johnson", guests.findById("P4826153").orElseThrow().getFullName());

        Reservation first = reservations.findById("SS-0001").orElseThrow();
        assertEquals("Nimal Perera", first.getGuest().getFullName());
        assertEquals("R201", first.getRoom().getRoomId());
        assertEquals(new DateRange(TODAY.plusDays(1), TODAY.plusDays(3)), first.getStay());
        assertEquals(2, first.getNumberOfGuests());

        Reservation second = reservations.findById("SS-0002").orElseThrow();
        assertEquals("Sarah Johnson", second.getGuest().getFullName());
        assertEquals("R301", second.getRoom().getRoomId());
        assertEquals(new DateRange(TODAY.plusDays(10), TODAY.plusDays(13)), second.getStay());
        assertEquals(2, second.getNumberOfGuests());

        assertEquals("SS-0003", idGenerator.next());
    }

    // Clock

    @Test
    @DisplayName("The clock can be fixed at a chosen date and time")
    void fixedClock() {
        Clock clock = SmartStayApp.createClock("2026-11-06T15:00", COLOMBO);
        assertEquals(LocalDateTime.of(2026, 11, 6, 15, 0), LocalDateTime.now(clock));
    }

    @Test
    @DisplayName("With no chosen time the clock uses the real time")
    void realClock() {
        assertEquals(Clock.system(COLOMBO), SmartStayApp.createClock(null, COLOMBO));
    }

    @Test
    @DisplayName("An invalid chosen time is rejected")
    void invalidClockRejected() {
        assertThrows(ValidationException.class, () -> SmartStayApp.createClock("tomorrow", COLOMBO));
    }

    // Navigation

    @Test
    @DisplayName("The application shows its banner and exits on 0")
    void startAndExit() {
        String output = run(BACK);
        assertTrue(output.contains("SmartStay Hotel Booking System"));
        assertTrue(output.contains("Goodbye."));
    }

    @Test
    @DisplayName("Text at a menu re-prompts")
    void textAtMenuReprompts() {
        assertTrue(run("abc", BACK).contains("Enter a number from 0 to 2."));
    }

    @Test
    @DisplayName("Running out of input ends the application cleanly")
    void inputClosedEndsCleanly() {
        assertTrue(run(GUEST_MENU).contains("Input closed. Goodbye."));
    }

    @Test
    @DisplayName("0 at a prompt returns to the menu")
    void zeroReturnsToMenu() {
        assertTrue(run(asGuest("1", BACK)).contains("Returning to the menu."));
    }

    // Admin

    @Test
    @DisplayName("A wrong admin password keeps the menu closed")
    void wrongPassword() {
        String output = run(ADMIN_MENU, "nope", BACK);
        assertTrue(output.contains("Incorrect password."));
        assertFalse(output.contains("Admin Menu"));
    }

    @Test
    @DisplayName("Admin sees all rooms sorted by ID, with rates and status")
    void viewAllRooms() {
        String output = run(asAdmin("5"));
        assertTrue(output.indexOf("R101") < output.indexOf("R402"));
        assertTrue(output.contains("LKR 18,000.00"));
        assertTrue(output.contains("Under Maintenance"));
    }

    @Test
    @DisplayName("Admin adds a room and sees its type's rate; a duplicate re-prompts")
    void addRoom() {
        String output = run(asAdmin("1", "R101", "R501", "9", "2", "4"));
        assertTrue(output.contains("Room R101 already exists."));
        assertTrue(output.contains("Room R501 added."));
        assertTrue(output.contains("LKR 9,000.00"));
    }

    @Test
    @DisplayName("Admin changes a bed type")
    void updateRoom() {
        String output = run(asAdmin("2", "R102", "1", "3"));
        assertTrue(output.contains("Room R102 updated."));
        assertTrue(output.contains("Queen"));
    }

    @Test
    @DisplayName("Answering N keeps the room; answering Y removes it")
    void removeRoomConfirmation() {
        String declined = run(asAdmin("3", "R102", "N", "5"));
        assertTrue(declined.contains("Room not removed."));
        assertTrue(declined.lastIndexOf("R102") > declined.indexOf("Room not removed."));

        String removed = run(asAdmin("3", "R102", "Y", "5"));
        assertTrue(removed.contains("Room R102 removed."));
        assertFalse(removed.substring(removed.indexOf("Room R102 removed.") + 20).contains("R102"));
    }

    @Test
    @DisplayName("A room with a future booking cannot be removed")
    void removeBookedRoomBlocked() {
        assertTrue(run(asAdmin("3", "R301")).contains("cannot be removed"));
    }

    @Test
    @DisplayName("A room put under maintenance disappears from the search")
    void maintenanceHidesRoom() {
        String[] adminThenSearch = {
            ADMIN_MENU, SmartStayApp.DEMO_ADMIN_PASSWORD, "4", "R202", "2", BACK,
            GUEST_MENU, "2", "2", day(20), day(22), BACK, BACK};
        String output = run(adminThenSearch);
        String search = output.substring(output.lastIndexOf("Room ID"));
        assertTrue(search.contains("R201"));
        assertFalse(search.contains("R202"));
    }

    @Test
    @DisplayName("Admin lists reservations in check-in date order")
    void viewAllReservations() {
        String output = run(asAdmin("6"));
        assertTrue(output.indexOf("SS-0001") < output.indexOf("SS-0002"));
    }

    // Guest registration

    @Test
    @DisplayName("Registration re-prompts on each bad value, then succeeds")
    void register() {
        String output = run(asGuest("1",
                "12345", "199012345678", "200012345678",
                "", "Kamal Silva",
                "12345", "0771234568",
                "kamal@", "kamal@example.com"));
        assertTrue(output.contains("Enter a NIC"));
        assertTrue(output.contains("already registered"));
        assertTrue(output.contains("Full name is required."));
        assertTrue(output.contains("Contact number must be"));
        assertTrue(output.contains("Enter an email address"));
        assertTrue(output.contains("Registered Kamal Silva (200012345678)."));
    }

    // Searching

    @Test
    @DisplayName("Search hides a booked room and shows it for a back-to-back stay")
    void searchRooms() {
        String overlapping = run(asGuest("2", "3", day(10), day(12)));
        assertTrue(overlapping.contains("R302"));
        assertFalse(overlapping.contains("R301"));

        assertTrue(run(asGuest("2", "3", day(13), day(15))).contains("R301"));
    }

    @Test
    @DisplayName("Check-out on the check-in date and a bad date both re-prompt")
    void searchDatePrompts() {
        String output = run(asGuest("2", "3", "2026/13/40", day(20), day(20), day(21)));
        assertTrue(output.contains("Enter a date in the format yyyy-MM-dd."));
        assertTrue(output.contains("Check-out date must be after the check-in date."));
    }

    // Booking

    @Test
    @DisplayName("A valid booking gets the next ID and charges the advance")
    void makeReservation() {
        String output = run(asGuest("3", "199012345678", "1", day(3), day(5), "R101", "0", "2", "Y"));
        assertTrue(output.contains("Enter a whole number of 1 or more."));
        assertTrue(output.contains("Booking confirmed: SS-0003. Advance charged: LKR 10,000.00."));
    }

    @Test
    @DisplayName("Answering N makes no booking")
    void declineBooking() {
        String output = run(asGuest("3", "199012345678", "1", day(3), day(5), "R101", "2", "N", "4", "SS-0003"));
        assertTrue(output.contains("Booking not made."));
        assertTrue(output.contains("Reservation SS-0003 was not found."));
    }

    @Test
    @DisplayName("Check-in tomorrow is rejected")
    void leadTimeRejected() {
        String output = run(asGuest("3", "199012345678", "1", day(1), day(2)));
        assertTrue(output.contains("at least 2 days in advance"));
    }

    @Test
    @DisplayName("An unregistered guest is told so")
    void unregisteredGuest() {
        String output = run(asGuest("3", "188888888V", BACK));
        assertTrue(output.contains("No guest is registered with number 188888888V."));
    }

    @Test
    @DisplayName("Rooms under maintenance, overlapping stays and too many guests are rejected")
    void bookingRulesRejected() {
        assertTrue(run(asGuest("3", "199012345678", "1", day(5), day(6), "R103", BACK))
                .contains("Room R103 is not available from"));
        assertTrue(run(asGuest("3", "199012345678", "3", day(11), day(12), "R301", BACK))
                .contains("Room R301 is not available from"));
        assertTrue(run(asGuest("3", "199012345678", "2", day(5), day(6), "R201", "5", "Y"))
                .contains("holds at most 4 guests"));
    }

    // Finding

    @Test
    @DisplayName("Reservations are found by ID, name and date, or reported as missing")
    void findReservations() {
        assertTrue(run(asGuest("4", "SS-0002")).contains("Sarah Johnson"));
        assertTrue(run(asGuest("4", "SS-9999")).contains("Reservation SS-9999 was not found."));
        assertTrue(run(asGuest("5", "nimal")).contains("SS-0001"));
        assertTrue(run(asGuest("5", "zzz")).contains("No reservations found for \"zzz\"."));
        assertTrue(run(asGuest("6", day(1))).contains("SS-0001"));
        assertTrue(run(asGuest("6", day(40))).contains("No reservations check in on"));
    }

    // Cancelling

    @Test
    @DisplayName("Cancelling refunds the advance, frees the room and cannot be repeated")
    void cancelReservation() {
        String output = run(asGuest("7", "SS-0002", "Y", "7", "SS-0002", "2", "3", day(10), day(12)));
        assertTrue(output.contains("Reservation SS-0002 cancelled. Advance of LKR 10,000.00 refunded."));
        assertTrue(output.contains("Reservation SS-0002 is already cancelled."));
        assertTrue(output.substring(output.lastIndexOf("Room ID")).contains("R301"));
    }

    @Test
    @DisplayName("Answering N keeps the reservation")
    void declineCancellation() {
        assertTrue(run(asGuest("7", "SS-0002", "N")).contains("Reservation not cancelled."));
    }

    @Test
    @DisplayName("SS-0001 cannot be cancelled once 14:00 has passed today")
    void cancelTooLate() {
        assertTrue(run(asGuest("7", "SS-0001")).contains("cannot be cancelled within 24 hours"));
    }

    @Test
    @DisplayName("SS-0001 can still be cancelled before 14:00 today")
    void cancelBeforeCutoff() {
        String output = runAt(TODAY.atTime(13, 59), asGuest("7", "SS-0001", "Y"));
        assertTrue(output.contains("Reservation SS-0001 cancelled."));
    }

    @Test
    @DisplayName("An unknown reservation cannot be cancelled")
    void cancelUnknown() {
        assertTrue(run(asGuest("7", "SS-9999")).contains("Reservation SS-9999 was not found."));
    }

    // Invoices

    @Test
    @DisplayName("An invoice shows every required field")
    void invoiceFields() {
        String output = run(asGuest("8", "SS-0002"));
        for (String expected : List.of("Sarah Johnson", "P4826153", "+447700900123", "sarah.johnson@example.com",
                "R301", "Suite", "King", "LKR 14,000.00", "Included", day(10), day(13), "Nights", "Guests",
                "Base room charge", "LKR 42,000.00", "Extra guest charges", "Discount",
                "Tax", "LKR 6,300.00", "Total", "LKR 48,300.00",
                "Advance deducted", "LKR 10,000.00", "Amount payable", "LKR 38,300.00")) {
            assertTrue(output.contains(expected), "missing: " + expected);
        }
    }

    @Test
    @DisplayName("An invoice shows a refund when the advance exceeds the total")
    void invoiceRefund() {
        String output = run(asGuest("3", "199012345678", "1", day(3), day(4), "R101", "1", "Y", "8", "SS-0003"));
        assertTrue(output.contains("Refund due"));
        assertTrue(output.contains("LKR 3,400.00"));
        assertTrue(output.contains("Not included"));
    }

    @Test
    @DisplayName("A cancelled reservation has no invoice")
    void cancelledHasNoInvoice() {
        assertTrue(run(asGuest("7", "SS-0002", "Y", "8", "SS-0002")).contains("has no invoice"));
    }
}
