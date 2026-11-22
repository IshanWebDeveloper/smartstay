package com.smartstay.repository;

import com.smartstay.algorithm.BinarySearch;
import com.smartstay.model.Reservation;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Reservations in three structures, each suited to one kind of lookup:
 * an {@link ArrayList} in booking-ID order for binary search by ID,
 * a {@link TreeMap} by check-in date for date lookups in date order,
 * and a {@link HashMap} by room ID for availability checks.
 */
public final class InMemoryReservationRepository implements Repository<Reservation, String> {

    // Shorter IDs first, so "SS-10000" still sorts after "SS-9999".
    private static final Comparator<String> BOOKING_ID_ORDER =
            Comparator.<String>comparingInt(String::length).thenComparing(Comparator.naturalOrder());

    private final List<Reservation> reservationsById = new ArrayList<>();
    private final TreeMap<LocalDate, List<Reservation>> reservationsByCheckIn = new TreeMap<>();
    private final Map<String, List<Reservation>> reservationsByRoom = new HashMap<>();

    /**
     * Adds a reservation, or replaces the one with the same ID.
     *
     * @param reservation the reservation to store
     * @throws IllegalArgumentException if a new ID does not come after every existing ID,
     *                                  which would break the sorted order
     */
    @Override
    public void save(Reservation reservation) {
        Objects.requireNonNull(reservation, "Reservation is required.");
        int index = indexOf(reservation.getBookingId());
        if (index == BinarySearch.NOT_FOUND) {
            requireAfterLastId(reservation.getBookingId());
            reservationsById.add(reservation);
        } else {
            removeFromIndexes(reservationsById.get(index));
            reservationsById.set(index, reservation);
        }
        addToIndexes(reservation);
    }

    @Override
    public Optional<Reservation> findById(String bookingId) {
        return BinarySearch.find(reservationsById, bookingId, Reservation::getBookingId, BOOKING_ID_ORDER);
    }

    /** @return all reservations in booking-ID order, as a list that cannot be changed */
    @Override
    public List<Reservation> findAll() {
        return List.copyOf(reservationsById);
    }

    @Override
    public boolean existsById(String bookingId) {
        return indexOf(bookingId) != BinarySearch.NOT_FOUND;
    }

    @Override
    public boolean deleteById(String bookingId) {
        int index = indexOf(bookingId);
        if (index == BinarySearch.NOT_FOUND) {
            return false;
        }
        removeFromIndexes(reservationsById.remove(index));
        return true;
    }

    /**
     * Finds the reservations that check in on a date.
     *
     * @param checkIn the check-in date
     * @return the reservations in booking-ID order, as a list that cannot be changed
     */
    public List<Reservation> findByCheckInDate(LocalDate checkIn) {
        return List.copyOf(reservationsByCheckIn.getOrDefault(checkIn, List.of()));
    }

    /** @return all reservations in check-in date order, as a list that cannot be changed */
    public List<Reservation> findAllOrderedByCheckIn() {
        List<Reservation> ordered = new ArrayList<>(reservationsById.size());
        reservationsByCheckIn.values().forEach(ordered::addAll);
        return List.copyOf(ordered);
    }

    /**
     * Finds the reservations for a room, including cancelled ones.
     *
     * @param roomId the room ID
     * @return the reservations in booking-ID order, as a list that cannot be changed
     */
    public List<Reservation> findByRoomId(String roomId) {
        return List.copyOf(reservationsByRoom.getOrDefault(roomId, List.of()));
    }

    private int indexOf(String bookingId) {
        return BinarySearch.indexOf(reservationsById, bookingId, Reservation::getBookingId, BOOKING_ID_ORDER);
    }

    private void requireAfterLastId(String bookingId) {
        if (reservationsById.isEmpty()) {
            return;
        }
        String lastId = reservationsById.get(reservationsById.size() - 1).getBookingId();
        if (BOOKING_ID_ORDER.compare(bookingId, lastId) <= 0) {
            throw new IllegalArgumentException("Booking ID " + bookingId + " must come after " + lastId + ".");
        }
    }

    private void addToIndexes(Reservation reservation) {
        reservationsByCheckIn.computeIfAbsent(reservation.getStay().checkIn(), date -> new ArrayList<>())
                .add(reservation);
        reservationsByRoom.computeIfAbsent(reservation.getRoom().getRoomId(), roomId -> new ArrayList<>())
                .add(reservation);
    }

    private void removeFromIndexes(Reservation reservation) {
        removeFromIndex(reservationsByCheckIn, reservation.getStay().checkIn(), reservation);
        removeFromIndex(reservationsByRoom, reservation.getRoom().getRoomId(), reservation);
    }

    // Drops the key once its list is empty, so the index holds no empty entries.
    private static <K> void removeFromIndex(Map<K, List<Reservation>> index, K key, Reservation reservation) {
        List<Reservation> bucket = index.get(key);
        bucket.remove(reservation);
        if (bucket.isEmpty()) {
            index.remove(key);
        }
    }
}
