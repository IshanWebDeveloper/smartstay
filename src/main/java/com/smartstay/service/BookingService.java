package com.smartstay.service;

import com.smartstay.exception.BookingException;
import com.smartstay.exception.NotFoundException;
import com.smartstay.exception.ValidationException;
import com.smartstay.model.DateRange;
import com.smartstay.model.Guest;
import com.smartstay.model.Reservation;
import com.smartstay.model.Room;
import com.smartstay.model.RoomType;
import com.smartstay.repository.InMemoryReservationRepository;
import com.smartstay.util.BookingIdGenerator;
import com.smartstay.validation.BookingRules;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Searches rooms, and makes, finds and cancels reservations.
 */
public class BookingService {

    private final RoomService roomService;
    private final GuestService guestService;
    private final InMemoryReservationRepository reservations;
    private final BookingRules rules;
    private final BookingIdGenerator idGenerator;

    /**
     * Creates the service.
     *
     * @param roomService  finds rooms
     * @param guestService finds guests
     * @param reservations the reservation store
     * @param rules        the booking rules
     * @param idGenerator  creates booking IDs
     */
    public BookingService(RoomService roomService, GuestService guestService,
                          InMemoryReservationRepository reservations, BookingRules rules,
                          BookingIdGenerator idGenerator) {
        this.roomService = Objects.requireNonNull(roomService, "Room service is required.");
        this.guestService = Objects.requireNonNull(guestService, "Guest service is required.");
        this.reservations = Objects.requireNonNull(reservations, "Reservation repository is required.");
        this.rules = Objects.requireNonNull(rules, "Booking rules are required.");
        this.idGenerator = Objects.requireNonNull(idGenerator, "Booking ID generator is required.");
    }

    /**
     * Finds the rooms of a type that can be booked for a stay.
     *
     * @param type the room type
     * @param stay the requested stay
     * @return the rooms, sorted by room ID
     */
    public List<Room> findAvailableRooms(RoomType type, DateRange stay) {
        List<Room> available = new ArrayList<>();
        for (Room room : roomService.listRooms()) {
            if (room.getType() == type
                    && rules.isRoomBookable(room, stay, reservations.findByRoomId(room.getRoomId()))) {
                available.add(room);
            }
        }
        return available;
    }

    /**
     * Makes a reservation and records the booking advance.
     *
     * @param nicOrPassport  the registered guest's number
     * @param roomId         the room ID
     * @param stay           the requested stay
     * @param numberOfGuests how many people will stay
     * @return the new reservation
     * @throws ValidationException if the guest number is malformed or there are no guests
     * @throws NotFoundException   if the guest or room does not exist
     * @throws BookingException    if a booking rule is broken
     */
    public Reservation createReservation(String nicOrPassport, String roomId, DateRange stay, int numberOfGuests) {
        Guest guest = guestService.findGuest(nicOrPassport);
        Room room = roomService.findRoom(roomId);
        rules.checkLeadTime(stay);
        rules.checkOccupancy(room.getType(), numberOfGuests);
        rules.checkRoomBookable(room, stay, reservations.findByRoomId(room.getRoomId()));

        // The ID is taken last, so a rejected booking does not leave a gap in the sequence.
        Reservation reservation = new Reservation(idGenerator.next(), guest, room, stay, numberOfGuests);
        reservations.save(reservation);
        return reservation;
    }

    /**
     * Finds a reservation by booking ID.
     *
     * @param bookingId the booking ID, in any case
     * @return the reservation
     * @throws ValidationException if the ID is blank
     * @throws NotFoundException   if the reservation does not exist
     */
    public Reservation findByBookingId(String bookingId) {
        if (bookingId == null || bookingId.isBlank()) {
            throw new ValidationException("Booking ID is required.");
        }
        String id = bookingId.trim().toUpperCase(Locale.ROOT);
        return reservations.findById(id)
                .orElseThrow(() -> new NotFoundException("Reservation " + id + " was not found."));
    }

    /**
     * Finds reservations whose guest name contains the given text, ignoring case.
     *
     * @param name the text to look for
     * @return the matches in booking-ID order, possibly empty
     * @throws ValidationException if the text is blank
     */
    public List<Reservation> findByGuestName(String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Guest name is required.");
        }
        String query = name.trim().toLowerCase(Locale.ROOT);
        List<Reservation> matches = new ArrayList<>();
        for (Reservation reservation : reservations.findAll()) {
            if (reservation.getGuest().getFullName().toLowerCase(Locale.ROOT).contains(query)) {
                matches.add(reservation);
            }
        }
        return matches;
    }

    /**
     * Finds the reservations that check in on a date.
     *
     * @param checkIn the check-in date
     * @return the reservations in booking-ID order, possibly empty
     */
    public List<Reservation> findByCheckInDate(LocalDate checkIn) {
        return reservations.findByCheckInDate(checkIn);
    }

    /** @return all reservations in check-in date order */
    public List<Reservation> listReservations() {
        return reservations.findAllOrderedByCheckIn();
    }

    /**
     * Cancels a reservation. The advance becomes refundable.
     *
     * @param bookingId the booking ID
     * @return the cancelled reservation
     * @throws NotFoundException if the reservation does not exist
     * @throws BookingException  if it is already cancelled or too close to check-in
     */
    public Reservation cancelReservation(String bookingId) {
        Reservation reservation = findByBookingId(bookingId);
        rules.checkCancellable(reservation);
        reservation.cancel();
        reservations.save(reservation);
        return reservation;
    }
}
