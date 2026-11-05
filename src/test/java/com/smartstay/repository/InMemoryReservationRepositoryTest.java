package com.smartstay.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.smartstay.model.BedType;
import com.smartstay.model.DateRange;
import com.smartstay.model.Guest;
import com.smartstay.model.Reservation;
import com.smartstay.model.Room;
import com.smartstay.model.RoomStatus;
import com.smartstay.model.RoomType;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Checks {@link InMemoryReservationRepository} and that its three structures stay in step.
 */
class InMemoryReservationRepositoryTest {

    private static final LocalDate DAY_10 = LocalDate.of(2026, 12, 10);
    private static final LocalDate DAY_20 = LocalDate.of(2026, 12, 20);
    private static final Guest GUEST =
            new Guest("199012345678", "Nimal Perera", "0771234567", "nimal@example.com");
    private static final Room R201 = new Room("R201", RoomType.DELUXE, BedType.QUEEN, RoomStatus.AVAILABLE);
    private static final Room R301 = new Room("R301", RoomType.SUITE, BedType.KING, RoomStatus.AVAILABLE);

    private final InMemoryReservationRepository repository = new InMemoryReservationRepository();

    private static Reservation booking(String id, Room room, LocalDate checkIn) {
        return new Reservation(id, GUEST, room, new DateRange(checkIn, checkIn.plusDays(2)), 2);
    }

    private static List<String> ids(List<Reservation> reservations) {
        return reservations.stream().map(Reservation::getBookingId).toList();
    }

    @Test
    @DisplayName("Finds first, middle and last by booking ID")
    void findById() {
        repository.save(booking("SS-0001", R201, DAY_10));
        repository.save(booking("SS-0002", R301, DAY_10));
        repository.save(booking("SS-0003", R201, DAY_20));

        assertEquals("SS-0001", repository.findById("SS-0001").orElseThrow().getBookingId());
        assertEquals("SS-0002", repository.findById("SS-0002").orElseThrow().getBookingId());
        assertEquals("SS-0003", repository.findById("SS-0003").orElseThrow().getBookingId());
    }

    @Test
    @DisplayName("An unknown booking ID finds nothing")
    void unknownId() {
        repository.save(booking("SS-0001", R201, DAY_10));

        assertTrue(repository.findById("SS-9999").isEmpty());
        assertFalse(repository.existsById("SS-9999"));
    }

    @Test
    @DisplayName("Lookup still works past SS-9999")
    void findPastFourDigits() {
        repository.save(booking("SS-9999", R201, DAY_10));
        repository.save(booking("SS-10000", R301, DAY_10));

        assertTrue(repository.existsById("SS-9999"));
        assertTrue(repository.existsById("SS-10000"));
        assertEquals(List.of("SS-9999", "SS-10000"), ids(repository.findAll()));
    }

    @Test
    @DisplayName("A new ID before the last one is rejected")
    void outOfOrderIdRejected() {
        repository.save(booking("SS-0002", R201, DAY_10));
        Reservation earlier = booking("SS-0001", R301, DAY_10);

        assertThrows(IllegalArgumentException.class, () -> repository.save(earlier));
        assertEquals(List.of("SS-0002"), ids(repository.findAll()));
    }

    @Test
    @DisplayName("Finds reservations by check-in date")
    void findByCheckInDate() {
        repository.save(booking("SS-0001", R201, DAY_10));
        repository.save(booking("SS-0002", R301, DAY_20));
        repository.save(booking("SS-0003", R301, DAY_10));

        assertEquals(List.of("SS-0001", "SS-0003"), ids(repository.findByCheckInDate(DAY_10)));
        assertTrue(repository.findByCheckInDate(DAY_10.plusDays(1)).isEmpty());
    }

    @Test
    @DisplayName("Lists all reservations in check-in date order")
    void orderedByCheckIn() {
        repository.save(booking("SS-0001", R201, DAY_20));
        repository.save(booking("SS-0002", R301, DAY_10));

        assertEquals(List.of("SS-0002", "SS-0001"), ids(repository.findAllOrderedByCheckIn()));
    }

    @Test
    @DisplayName("Finds reservations by room, including cancelled ones")
    void findByRoomId() {
        Reservation cancelled = booking("SS-0001", R201, DAY_10);
        cancelled.cancel();
        repository.save(cancelled);
        repository.save(booking("SS-0002", R301, DAY_10));
        repository.save(booking("SS-0003", R201, DAY_20));

        assertEquals(List.of("SS-0001", "SS-0003"), ids(repository.findByRoomId("R201")));
        assertTrue(repository.findByRoomId("R999").isEmpty());
    }

    @Test
    @DisplayName("Saving the same ID replaces it in every lookup")
    void saveReplacesEverywhere() {
        repository.save(booking("SS-0001", R201, DAY_10));
        repository.save(booking("SS-0001", R301, DAY_20));

        assertEquals(1, repository.findAll().size());
        assertTrue(repository.findByCheckInDate(DAY_10).isEmpty());
        assertTrue(repository.findByRoomId("R201").isEmpty());
        assertEquals(List.of("SS-0001"), ids(repository.findByCheckInDate(DAY_20)));
        assertEquals(List.of("SS-0001"), ids(repository.findByRoomId("R301")));
    }

    @Test
    @DisplayName("Replacing a reservation keeps the lookups in booking-ID order")
    void replaceKeepsOrder() {
        repository.save(booking("SS-0001", R201, DAY_10));
        repository.save(booking("SS-0002", R201, DAY_10));
        repository.save(booking("SS-0001", R201, DAY_10));

        assertEquals(List.of("SS-0001", "SS-0002"), ids(repository.findByCheckInDate(DAY_10)));
        assertEquals(List.of("SS-0001", "SS-0002"), ids(repository.findByRoomId("R201")));
    }

    @Test
    @DisplayName("Deleting removes it from every lookup")
    void deleteRemovesEverywhere() {
        repository.save(booking("SS-0001", R201, DAY_10));

        assertTrue(repository.deleteById("SS-0001"));
        assertFalse(repository.existsById("SS-0001"));
        assertTrue(repository.findByCheckInDate(DAY_10).isEmpty());
        assertTrue(repository.findByRoomId("R201").isEmpty());
        assertTrue(repository.findAllOrderedByCheckIn().isEmpty());
        assertFalse(repository.deleteById("SS-0001"));
    }

    @Test
    @DisplayName("The lists returned cannot change the repository")
    void returnedListsUnmodifiable() {
        Reservation reservation = booking("SS-0001", R201, DAY_10);
        repository.save(reservation);
        List<Reservation> all = repository.findAll();
        List<Reservation> byDate = repository.findByCheckInDate(DAY_10);
        List<Reservation> byRoom = repository.findByRoomId("R201");

        assertThrows(UnsupportedOperationException.class, all::clear);
        assertThrows(UnsupportedOperationException.class, byDate::clear);
        assertThrows(UnsupportedOperationException.class, byRoom::clear);
    }
}
