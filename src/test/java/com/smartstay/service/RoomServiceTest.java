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
import com.smartstay.model.Guest;
import com.smartstay.model.Reservation;
import com.smartstay.model.Room;
import com.smartstay.model.RoomStatus;
import com.smartstay.model.RoomType;
import com.smartstay.repository.InMemoryReservationRepository;
import com.smartstay.repository.InMemoryRoomRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Checks {@link RoomService}.
 */
class RoomServiceTest {

    private static final ZoneId COLOMBO = ZoneId.of("Asia/Colombo");
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 5);
    private static final Guest GUEST =
            new Guest("199012345678", "Nimal Perera", "0771234567", "nimal@example.com");

    private final InMemoryRoomRepository rooms = new InMemoryRoomRepository();
    private final InMemoryReservationRepository reservations = new InMemoryReservationRepository();
    private final RoomService service = new RoomService(rooms, reservations,
            Clock.fixed(TODAY.atTime(12, 0).atZone(COLOMBO).toInstant(), COLOMBO));

    private Room addStandard(String id) {
        return service.addRoom(id, RoomType.STANDARD, BedType.DOUBLE);
    }

    private Reservation book(Room room, int checkInOffset, int nights) {
        LocalDate checkIn = TODAY.plusDays(checkInOffset);
        Reservation reservation = new Reservation("SS-0001", GUEST, room,
                new DateRange(checkIn, checkIn.plusDays(nights)), 1);
        reservations.save(reservation);
        return reservation;
    }

    @Test
    @DisplayName("A new room is available and shows its type's rate")
    void addRoom() {
        Room room = service.addRoom("r501", RoomType.DELUXE, BedType.KING);

        assertEquals("R501", room.getRoomId());
        assertEquals(RoomStatus.AVAILABLE, room.getStatus());
        assertEquals(new BigDecimal("9000.00"), room.getDailyRate());
        assertTrue(rooms.existsById("R501"));
    }

    @Test
    @DisplayName("A duplicate room ID is rejected, whatever the case")
    void duplicateRejected() {
        addStandard("R101");
        assertThrows(ValidationException.class, () -> addStandard("r101"));
    }

    @Test
    @DisplayName("A malformed room ID is rejected")
    void malformedIdRejected() {
        assertThrows(ValidationException.class, () -> addStandard(" "));
        assertThrows(ValidationException.class, () -> addStandard("R-101"));
        assertThrows(ValidationException.class, () -> addStandard(null));
    }

    @Test
    @DisplayName("The bed can be updated while the room has a booking")
    void updateBedWithBooking() {
        Room room = addStandard("R102");
        book(room, 10, 2);

        service.updateRoom("R102", RoomType.STANDARD, BedType.QUEEN);

        assertEquals(BedType.QUEEN, room.getBedType());
    }

    @Test
    @DisplayName("The type cannot change while the room has a future booking")
    void updateTypeBlockedByBooking() {
        Room room = addStandard("R102");
        book(room, 10, 2);

        assertThrows(BookingException.class,
                () -> service.updateRoom("R102", RoomType.SUITE, BedType.DOUBLE));
        assertEquals(RoomType.STANDARD, room.getType());
    }

    @Test
    @DisplayName("The type can change when the room has no booking")
    void updateTypeWithoutBooking() {
        Room room = addStandard("R102");

        service.updateRoom("R102", RoomType.SUITE, BedType.KING);

        assertEquals(RoomType.SUITE, room.getType());
        assertEquals(BedType.KING, room.getBedType());
    }

    @Test
    @DisplayName("Status can be changed")
    void changeStatus() {
        addStandard("R202");
        assertTrue(service.changeStatus("R202", RoomStatus.UNDER_MAINTENANCE).isUnderMaintenance());
    }

    @Test
    @DisplayName("A room with no bookings can be removed")
    void removeRoom() {
        addStandard("R102");
        service.removeRoom("R102");
        assertFalse(rooms.existsById("R102"));
    }

    @Test
    @DisplayName("A room with a future booking cannot be removed")
    void removeBlockedByFutureBooking() {
        Room room = addStandard("R301");
        book(room, 10, 3);

        assertThrows(BookingException.class, () -> service.removeRoom("R301"));
        assertTrue(rooms.existsById("R301"));
    }

    @Test
    @DisplayName("A room with a guest staying now cannot be removed")
    void removeBlockedByCurrentStay() {
        Room room = addStandard("R301");
        book(room, -1, 3);

        assertThrows(BookingException.class, () -> service.removeRoom("R301"));
    }

    @Test
    @DisplayName("A room whose booking was cancelled can be removed")
    void removeAllowedAfterCancellation() {
        Room room = addStandard("R301");
        book(room, 10, 3).cancel();

        service.removeRoom("R301");

        assertFalse(rooms.existsById("R301"));
    }

    @Test
    @DisplayName("A room whose guest left today can be removed")
    void removeAllowedAfterCheckOut() {
        Room room = addStandard("R301");
        book(room, -3, 3);

        service.removeRoom("R301");

        assertFalse(rooms.existsById("R301"));
    }

    @Test
    @DisplayName("Rooms are listed in ID order")
    void listSorted() {
        addStandard("R301");
        addStandard("R101");
        addStandard("R201");

        List<String> ids = service.listRooms().stream().map(Room::getRoomId).toList();

        assertEquals(List.of("R101", "R201", "R301"), ids);
    }

    @Test
    @DisplayName("An unknown room is reported")
    void unknownRoom() {
        assertThrows(NotFoundException.class, () -> service.findRoom("R999"));
        assertThrows(NotFoundException.class, () -> service.removeRoom("R999"));
        assertThrows(NotFoundException.class, () -> service.changeStatus("R999", RoomStatus.AVAILABLE));
    }
}
