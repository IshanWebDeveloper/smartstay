package com.smartstay.service;

import com.smartstay.algorithm.MergeSort;
import com.smartstay.exception.BookingException;
import com.smartstay.exception.NotFoundException;
import com.smartstay.exception.ValidationException;
import com.smartstay.model.BedType;
import com.smartstay.model.Room;
import com.smartstay.model.RoomStatus;
import com.smartstay.model.RoomType;
import com.smartstay.repository.InMemoryReservationRepository;
import com.smartstay.repository.Repository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Administrator operations on room records.
 */
public class RoomService {

    private static final Pattern ROOM_ID = Pattern.compile("[A-Z0-9]{2,10}");

    private final Repository<Room, String> rooms;
    private final InMemoryReservationRepository reservations;
    private final Clock clock;

    /**
     * Creates the service.
     *
     * @param rooms        the room store
     * @param reservations the reservation store
     * @param clock        the source of today's date
     */
    public RoomService(Repository<Room, String> rooms, InMemoryReservationRepository reservations, Clock clock) {
        this.rooms = Objects.requireNonNull(rooms, "Room repository is required.");
        this.reservations = Objects.requireNonNull(reservations, "Reservation repository is required.");
        this.clock = Objects.requireNonNull(clock, "Clock is required.");
    }

    /**
     * Adds an available room.
     *
     * @param roomId  the new room ID
     * @param type    the room type
     * @param bedType the bed type
     * @return the new room
     * @throws ValidationException if the ID is malformed or already used
     */
    public Room addRoom(String roomId, RoomType type, BedType bedType) {
        String id = normaliseId(roomId);
        if (rooms.existsById(id)) {
            throw new ValidationException("Room " + id + " already exists.");
        }
        Room room = new Room(id, type, bedType, RoomStatus.AVAILABLE);
        rooms.save(room);
        return room;
    }

    /**
     * Changes a room's type and bed.
     *
     * @param roomId  the room ID
     * @param type    the new room type
     * @param bedType the new bed type
     * @return the updated room
     * @throws NotFoundException if the room does not exist
     * @throws BookingException  if the type changes while the room has a current or future
     *                           booking, which would change that booking's price
     */
    public Room updateRoom(String roomId, RoomType type, BedType bedType) {
        Room room = findRoom(roomId);
        if (type != room.getType() && hasCurrentOrFutureBooking(room)) {
            throw new BookingException("The type of room " + room.getRoomId()
                    + " cannot change while it has a current or future booking.");
        }
        room.changeType(type);
        room.changeBedType(bedType);
        return room;
    }

    /**
     * Removes a room.
     *
     * @param roomId the room ID
     * @throws NotFoundException if the room does not exist
     * @throws BookingException  if the room has a current or future booking
     */
    public void removeRoom(String roomId) {
        Room room = findRoom(roomId);
        if (hasCurrentOrFutureBooking(room)) {
            throw new BookingException("Room " + room.getRoomId()
                    + " cannot be removed while it has a current or future booking.");
        }
        rooms.deleteById(room.getRoomId());
    }

    /**
     * Sets a room's status.
     *
     * @param roomId the room ID
     * @param status the new status
     * @return the updated room
     * @throws NotFoundException if the room does not exist
     */
    public Room changeStatus(String roomId, RoomStatus status) {
        Room room = findRoom(roomId);
        room.changeStatus(status);
        return room;
    }

    /** @return all rooms, sorted by room ID */
    public List<Room> listRooms() {
        return MergeSort.sort(rooms.findAll(), Comparator.comparing(Room::getRoomId));
    }

    /**
     * Finds a room by ID.
     *
     * @param roomId the room ID, in any case
     * @return the room
     * @throws NotFoundException if the room does not exist
     */
    public Room findRoom(String roomId) {
        String id = normaliseId(roomId);
        return rooms.findById(id)
                .orElseThrow(() -> new NotFoundException("Room " + id + " was not found."));
    }

    private boolean hasCurrentOrFutureBooking(Room room) {
        LocalDate today = LocalDate.now(clock);
        return reservations.findByRoomId(room.getRoomId()).stream()
                .anyMatch(reservation -> reservation.isActive()
                        && reservation.getStay().checkOut().isAfter(today));
    }

    private static String normaliseId(String roomId) {
        String id = roomId == null ? "" : roomId.trim().toUpperCase(Locale.ROOT);
        if (!ROOM_ID.matcher(id).matches()) {
            throw new ValidationException("Room ID must be 2 to 10 letters or digits, like R501.");
        }
        return id;
    }
}
