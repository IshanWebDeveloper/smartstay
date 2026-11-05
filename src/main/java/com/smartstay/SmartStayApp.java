package com.smartstay;

import com.smartstay.exception.ValidationException;
import com.smartstay.model.Admin;
import com.smartstay.model.BedType;
import com.smartstay.model.DateRange;
import com.smartstay.model.Guest;
import com.smartstay.model.Reservation;
import com.smartstay.model.Room;
import com.smartstay.model.RoomStatus;
import com.smartstay.model.RoomType;
import com.smartstay.pricing.BillingCalculator;
import com.smartstay.pricing.LongStayDiscount;
import com.smartstay.repository.InMemoryGuestRepository;
import com.smartstay.repository.InMemoryReservationRepository;
import com.smartstay.repository.InMemoryRoomRepository;
import com.smartstay.service.BookingService;
import com.smartstay.service.GuestService;
import com.smartstay.service.InvoiceService;
import com.smartstay.service.RoomService;
import com.smartstay.ui.AdminMenu;
import com.smartstay.ui.GuestMenu;
import com.smartstay.ui.InputReader;
import com.smartstay.ui.MainMenu;
import com.smartstay.ui.TablePrinter;
import com.smartstay.util.BookingIdGenerator;
import com.smartstay.validation.BookingRules;
import com.smartstay.validation.GuestValidator;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

/**
 * Entry point and composition root: creates every object and passes in what it needs.
 */
public final class SmartStayApp {

    /** Demo password for the admin menu. */
    static final String DEMO_ADMIN_PASSWORD = "admin123";

    /** System property that fixes the date and time for manual testing. */
    static final String TEST_NOW_PROPERTY = "smartstay.now";

    private SmartStayApp() {
    }

    /**
     * Starts the console application.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        TablePrinter printer = TablePrinter.forConsole();
        try {
            Clock clock = createClock(System.getProperty(TEST_NOW_PROPERTY), ZoneId.systemDefault());
            createMainMenu(clock, InputReader.forConsole(printer), printer).start();
        } catch (ValidationException e) {
            printer.printError(e.getMessage());
        }
    }

    /**
     * Creates the application clock.
     *
     * @param testNow a date and time like 2026-11-06T15:00 to fix the clock at, or {@code null}
     *                to use the real time
     * @param zone    the time zone
     * @return the clock
     * @throws ValidationException if {@code testNow} is not a valid date and time
     */
    static Clock createClock(String testNow, ZoneId zone) {
        if (testNow == null) {
            return Clock.system(zone);
        }
        try {
            return Clock.fixed(LocalDateTime.parse(testNow).atZone(zone).toInstant(), zone);
        } catch (DateTimeParseException e) {
            throw new ValidationException(
                    TEST_NOW_PROPERTY + " must be a date and time like 2026-11-06T15:00.");
        }
    }

    /**
     * Wires the application together and loads the seed data.
     *
     * @param clock   the source of today's date and the current time
     * @param input   reads the user's input
     * @param printer writes the output
     * @return the main menu, ready to start
     */
    static MainMenu createMainMenu(Clock clock, InputReader input, TablePrinter printer) {
        InMemoryRoomRepository rooms = new InMemoryRoomRepository();
        InMemoryGuestRepository guests = new InMemoryGuestRepository();
        InMemoryReservationRepository reservations = new InMemoryReservationRepository();
        BookingIdGenerator idGenerator = new BookingIdGenerator();
        loadSeedData(rooms, guests, reservations, idGenerator, LocalDate.now(clock));

        GuestValidator validator = new GuestValidator();
        RoomService roomService = new RoomService(rooms, reservations, clock);
        GuestService guestService = new GuestService(guests, validator);
        BookingService bookingService = new BookingService(
                roomService, guestService, reservations, new BookingRules(clock), idGenerator);
        InvoiceService invoiceService = new InvoiceService(
                bookingService, new BillingCalculator(new LongStayDiscount()));

        GuestMenu guestMenu = new GuestMenu(
                guestService, validator, roomService, bookingService, invoiceService, input, printer);
        AdminMenu adminMenu = new AdminMenu(
                new Admin("admin", "Hotel Administrator", DEMO_ADMIN_PASSWORD),
                roomService, bookingService, input, printer);
        return new MainMenu(guestMenu, adminMenu, input, printer);
    }

    /**
     * Loads the starting data. Reservations go straight into the repository, skipping
     * the booking rules on purpose, so SS-0001 can check in tomorrow.
     *
     * @param rooms        the room store
     * @param guests       the guest store
     * @param reservations the reservation store
     * @param idGenerator  creates booking IDs
     * @param today        the date the application started
     */
    static void loadSeedData(InMemoryRoomRepository rooms, InMemoryGuestRepository guests,
                             InMemoryReservationRepository reservations, BookingIdGenerator idGenerator,
                             LocalDate today) {
        rooms.save(new Room("R101", RoomType.STANDARD, BedType.SINGLE, RoomStatus.AVAILABLE));
        rooms.save(new Room("R102", RoomType.STANDARD, BedType.DOUBLE, RoomStatus.AVAILABLE));
        rooms.save(new Room("R103", RoomType.STANDARD, BedType.DOUBLE, RoomStatus.UNDER_MAINTENANCE));
        Room r201 = new Room("R201", RoomType.DELUXE, BedType.QUEEN, RoomStatus.AVAILABLE);
        rooms.save(r201);
        rooms.save(new Room("R202", RoomType.DELUXE, BedType.KING, RoomStatus.AVAILABLE));
        Room r301 = new Room("R301", RoomType.SUITE, BedType.KING, RoomStatus.AVAILABLE);
        rooms.save(r301);
        rooms.save(new Room("R302", RoomType.SUITE, BedType.QUEEN, RoomStatus.AVAILABLE));
        rooms.save(new Room("R401", RoomType.FAMILY_SUITE, BedType.KING, RoomStatus.AVAILABLE));
        rooms.save(new Room("R402", RoomType.FAMILY_SUITE, BedType.QUEEN, RoomStatus.AVAILABLE));

        Guest nimal = new Guest("199012345678", "Nimal Perera", "0771234567", "nimal.perera@example.com");
        Guest sarah = new Guest("P4826153", "Sarah Johnson", "+447700900123", "sarah.johnson@example.com");
        guests.save(nimal);
        guests.save(sarah);

        LocalDate tomorrow = today.plusDays(1);
        LocalDate inTenDays = today.plusDays(10);
        reservations.save(new Reservation(idGenerator.next(), nimal, r201,
                new DateRange(tomorrow, tomorrow.plusDays(2)), 2));
        reservations.save(new Reservation(idGenerator.next(), sarah, r301,
                new DateRange(inTenDays, inTenDays.plusDays(3)), 2));
    }
}
