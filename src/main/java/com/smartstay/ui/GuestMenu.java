package com.smartstay.ui;

import com.smartstay.exception.ValidationException;
import com.smartstay.model.DateRange;
import com.smartstay.model.Guest;
import com.smartstay.model.Reservation;
import com.smartstay.model.Room;
import com.smartstay.model.RoomType;
import com.smartstay.service.BookingService;
import com.smartstay.service.GuestService;
import com.smartstay.service.InvoiceService;
import com.smartstay.service.RoomService;
import com.smartstay.util.DateFormat;
import com.smartstay.util.MoneyFormat;
import com.smartstay.validation.GuestValidator;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Guest options: register, search rooms, and make, find, cancel and invoice reservations.
 */
public final class GuestMenu extends Menu {

    private static final List<RoomType> ROOM_TYPES = List.of(RoomType.values());

    private final GuestService guestService;
    private final GuestValidator validator;
    private final RoomService roomService;
    private final BookingService bookingService;
    private final InvoiceService invoiceService;

    /**
     * Creates the menu.
     *
     * @param guestService   registers and finds guests
     * @param validator      checks guest details as they are typed
     * @param roomService    finds rooms
     * @param bookingService makes and finds reservations
     * @param invoiceService creates invoices
     * @param input          reads the user's input
     * @param printer        writes the output
     */
    public GuestMenu(GuestService guestService, GuestValidator validator, RoomService roomService,
                     BookingService bookingService, InvoiceService invoiceService,
                     InputReader input, TablePrinter printer) {
        super(input, printer);
        this.guestService = Objects.requireNonNull(guestService, "Guest service is required.");
        this.validator = Objects.requireNonNull(validator, "Guest validator is required.");
        this.roomService = Objects.requireNonNull(roomService, "Room service is required.");
        this.bookingService = Objects.requireNonNull(bookingService, "Booking service is required.");
        this.invoiceService = Objects.requireNonNull(invoiceService, "Invoice service is required.");
    }

    @Override
    protected String title() {
        return "Guest Menu";
    }

    @Override
    protected List<Option> options() {
        return List.of(
                new Option("Register", this::register),
                new Option("Search available rooms", this::searchRooms),
                new Option("Make a reservation", this::makeReservation),
                new Option("Find a reservation by booking ID", this::findByBookingId),
                new Option("Find reservations by guest name", this::findByGuestName),
                new Option("View reservations by check-in date", this::viewByCheckInDate),
                new Option("Cancel a reservation", this::cancelReservation),
                new Option("View an invoice", this::viewInvoice));
    }

    private void register() {
        String id = input.read("NIC or passport number", this::checkNewGuestNumber);
        String name = input.read("Full name", validator::checkFullName);
        String phone = input.read("Contact number", validator::checkContactNumber);
        String email = input.read("Email", validator::checkEmail);

        Guest guest = guestService.register(id, name, phone, email);
        printer.printMessage("Registered " + guest.getFullName() + " (" + guest.getNicOrPassport() + ").");
    }

    private String checkNewGuestNumber(String text) {
        String id = validator.checkNicOrPassport(text);
        if (guestService.isRegistered(id)) {
            throw new ValidationException("A guest with number " + id + " is already registered.");
        }
        return id;
    }

    private void searchRooms() {
        RoomType type = input.readOption("Room type", ROOM_TYPES);
        DateRange stay = readStay();
        List<Room> rooms = bookingService.findAvailableRooms(type, stay);
        if (rooms.isEmpty()) {
            printer.printMessage("No " + type + " rooms are available from " + stay + ".");
            return;
        }
        printer.printRooms(rooms);
    }

    private void makeReservation() {
        Guest guest = input.read("NIC or passport number", guestService::findGuest);
        RoomType type = input.readOption("Room type", ROOM_TYPES);
        DateRange stay = readStay();
        bookingService.checkLeadTime(stay);

        List<Room> rooms = bookingService.findAvailableRooms(type, stay);
        if (rooms.isEmpty()) {
            printer.printMessage("No " + type + " rooms are available from " + stay + ".");
            return;
        }
        printer.printRooms(rooms);
        Room room = input.read("Room ID", text -> findRoomOfType(text, type));
        int numberOfGuests = input.readPositiveNumber("Number of guests");

        printer.printMessage(guest.getFullName() + ", room " + room.getRoomId() + ", " + stay + ", "
                + numberOfGuests + " guest(s). A refundable advance of "
                + MoneyFormat.format(Reservation.BOOKING_ADVANCE) + " will be charged.");
        if (!input.confirm("Confirm this booking?")) {
            printer.printMessage("Booking not made.");
            return;
        }
        Reservation reservation = bookingService.createReservation(
                guest.getNicOrPassport(), room.getRoomId(), stay, numberOfGuests);
        printer.printMessage("Booking confirmed: " + reservation.getBookingId() + ". Advance charged: "
                + MoneyFormat.format(reservation.getAdvancePaid()) + ".");
        printer.printReservations(List.of(reservation));
    }

    private Room findRoomOfType(String roomId, RoomType type) {
        Room room = roomService.findRoom(roomId);
        if (room.getType() != type) {
            throw new ValidationException("Room " + room.getRoomId() + " is not a " + type + " room.");
        }
        return room;
    }

    private DateRange readStay() {
        LocalDate checkIn = input.readDate("Check-in date");
        return input.read("Check-out date (" + DateFormat.DISPLAY_PATTERN + ")",
                text -> new DateRange(checkIn, DateFormat.parse(text)));
    }

    private void findByBookingId() {
        String bookingId = input.readText("Booking ID");
        printer.printReservations(List.of(bookingService.findByBookingId(bookingId)));
    }

    private void findByGuestName() {
        String name = input.readText("Guest name");
        printReservationsOrMessage(bookingService.findByGuestName(name),
                "No reservations found for \"" + name + "\".");
    }

    private void viewByCheckInDate() {
        LocalDate checkIn = input.readDate("Check-in date");
        printReservationsOrMessage(bookingService.findByCheckInDate(checkIn),
                "No reservations check in on " + DateFormat.format(checkIn) + ".");
    }

    private void printReservationsOrMessage(List<Reservation> reservations, String emptyMessage) {
        if (reservations.isEmpty()) {
            printer.printMessage(emptyMessage);
            return;
        }
        printer.printReservations(reservations);
    }

    private void cancelReservation() {
        String bookingId = input.readText("Booking ID");
        Reservation reservation = bookingService.checkCancellable(bookingId);
        printer.printReservations(List.of(reservation));
        if (!input.confirm("Cancel this reservation?")) {
            printer.printMessage("Reservation not cancelled.");
            return;
        }
        bookingService.cancelReservation(bookingId);
        printer.printMessage("Reservation " + reservation.getBookingId() + " cancelled. Advance of "
                + MoneyFormat.format(reservation.getAdvancePaid()) + " refunded.");
    }

    private void viewInvoice() {
        String bookingId = input.readText("Booking ID");
        printer.printInvoice(invoiceService.generateInvoice(bookingId));
    }
}
