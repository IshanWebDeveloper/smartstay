package com.smartstay.ui;

import com.smartstay.model.Admin;
import com.smartstay.model.BedType;
import com.smartstay.model.Reservation;
import com.smartstay.model.Room;
import com.smartstay.model.RoomStatus;
import com.smartstay.model.RoomType;
import com.smartstay.service.BookingService;
import com.smartstay.service.RoomService;
import java.util.List;
import java.util.Objects;

/**
 * Administrator options, behind a password: manage rooms and view all reservations.
 */
public final class AdminMenu extends Menu {

    private static final List<RoomType> ROOM_TYPES = List.of(RoomType.values());
    private static final List<BedType> BED_TYPES = List.of(BedType.values());
    private static final List<RoomStatus> ROOM_STATUSES = List.of(RoomStatus.values());

    private final Admin admin;
    private final RoomService roomService;
    private final BookingService bookingService;

    /**
     * Creates the menu.
     *
     * @param admin          the administrator who may sign in
     * @param roomService    manages rooms
     * @param bookingService lists reservations
     * @param input          reads the user's input
     * @param printer        writes the output
     */
    public AdminMenu(Admin admin, RoomService roomService, BookingService bookingService,
                     InputReader input, TablePrinter printer) {
        super(input, printer);
        this.admin = Objects.requireNonNull(admin, "Admin is required.");
        this.roomService = Objects.requireNonNull(roomService, "Room service is required.");
        this.bookingService = Objects.requireNonNull(bookingService, "Booking service is required.");
    }

    /** Asks for the password, then runs the menu if it is correct. */
    @Override
    public void run() {
        if (!admin.isPasswordCorrect(input.readLine("Admin password"))) {
            printer.printError("Incorrect password.");
            return;
        }
        super.run();
    }

    @Override
    protected String title() {
        return "Admin Menu";
    }

    @Override
    protected List<Option> options() {
        return List.of(
                new Option("Add a room", this::addRoom),
                new Option("Update a room", this::updateRoom),
                new Option("Remove a room", this::removeRoom),
                new Option("Change a room's status", this::changeStatus),
                new Option("View all rooms", this::viewRooms),
                new Option("View all reservations", this::viewReservations));
    }

    private void addRoom() {
        String roomId = input.read("New room ID", roomService::checkNewRoomId);
        RoomType type = input.readOption("Room type", ROOM_TYPES);
        BedType bedType = input.readOption("Bed type", BED_TYPES);

        Room room = roomService.addRoom(roomId, type, bedType);
        printer.printMessage("Room " + room.getRoomId() + " added.");
        printer.printRooms(List.of(room));
    }

    private void updateRoom() {
        Room room = input.read("Room ID", roomService::findRoom);
        printer.printRooms(List.of(room));
        RoomType type = input.readOption("New room type", ROOM_TYPES);
        BedType bedType = input.readOption("New bed type", BED_TYPES);

        roomService.updateRoom(room.getRoomId(), type, bedType);
        printer.printMessage("Room " + room.getRoomId() + " updated.");
        printer.printRooms(List.of(room));
    }

    private void removeRoom() {
        Room room = input.read("Room ID", roomService::findRoom);
        roomService.checkRemovable(room.getRoomId());
        printer.printRooms(List.of(room));
        if (!input.confirm("Remove this room?")) {
            printer.printMessage("Room not removed.");
            return;
        }
        roomService.removeRoom(room.getRoomId());
        printer.printMessage("Room " + room.getRoomId() + " removed.");
    }

    private void changeStatus() {
        Room room = input.read("Room ID", roomService::findRoom);
        RoomStatus status = input.readOption("New status", ROOM_STATUSES);

        roomService.changeStatus(room.getRoomId(), status);
        printer.printMessage("Room " + room.getRoomId() + " is now " + status + ".");
    }

    private void viewRooms() {
        printer.printRooms(roomService.listRooms());
    }

    private void viewReservations() {
        List<Reservation> reservations = bookingService.listReservations();
        if (reservations.isEmpty()) {
            printer.printMessage("There are no reservations.");
            return;
        }
        printer.printReservations(reservations);
    }
}
