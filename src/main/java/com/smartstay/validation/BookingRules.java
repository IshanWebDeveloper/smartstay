package com.smartstay.validation;

import com.smartstay.exception.BookingException;
import com.smartstay.exception.ValidationException;
import com.smartstay.model.DateRange;
import com.smartstay.model.Reservation;
import com.smartstay.model.Room;
import com.smartstay.model.RoomType;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Objects;

/**
 * Business rules for making and cancelling bookings. "Today" and "now" come from the
 * injected clock, so the rules can be tested at a fixed time.
 */
public class BookingRules {

    /** Fewest days between today and check-in. */
    public static final int MIN_LEAD_DAYS = 2;

    /** Hours before check-in after which a booking can no longer be cancelled. */
    public static final int CANCELLATION_CUTOFF_HOURS = 24;

    /** Time guests check in. */
    public static final LocalTime STANDARD_CHECK_IN_TIME = LocalTime.of(14, 0);

    private final Clock clock;

    /**
     * Creates the rules.
     *
     * @param clock the source of today's date and the current time
     */
    public BookingRules(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "Clock is required.");
    }

    /** @return the earliest check-in date allowed today */
    public LocalDate earliestCheckIn() {
        return LocalDate.now(clock).plusDays(MIN_LEAD_DAYS);
    }

    /**
     * Checks the stay starts at least {@link #MIN_LEAD_DAYS} days from today.
     *
     * @param stay the requested stay
     * @throws BookingException if check-in is too soon
     */
    public void checkLeadTime(DateRange stay) {
        LocalDate earliest = earliestCheckIn();
        if (stay.checkIn().isBefore(earliest)) {
            throw new BookingException("Bookings must be made at least " + MIN_LEAD_DAYS
                    + " days in advance. The earliest check-in is " + earliest + ".");
        }
    }

    /**
     * Checks the number of guests fits the room type.
     *
     * @param type           the room type
     * @param numberOfGuests the guests on the booking
     * @throws ValidationException if there are no guests
     * @throws BookingException    if there are more guests than the room can hold
     */
    public void checkOccupancy(RoomType type, int numberOfGuests) {
        if (numberOfGuests < 1) {
            throw new ValidationException("Number of guests must be at least 1.");
        }
        if (numberOfGuests > type.getMaxOccupancy()) {
            throw new BookingException("A " + type + " room holds at most " + type.getMaxOccupancy()
                    + " guests (" + type.getCapacity() + " plus " + RoomType.MAX_EXTRA_GUESTS + " extra).");
        }
    }

    /**
     * Checks a room can be booked for a stay.
     *
     * @param room             the room
     * @param stay             the requested stay
     * @param roomReservations the existing reservations for that room
     * @throws BookingException if the room is under maintenance or already reserved
     */
    public void checkRoomBookable(Room room, DateRange stay, Collection<Reservation> roomReservations) {
        if (room.isUnderMaintenance()) {
            throw new BookingException("Room " + room.getRoomId() + " is under maintenance.");
        }
        for (Reservation reservation : roomReservations) {
            if (reservation.blocksRoomDuring(stay)) {
                throw new BookingException("Room " + room.getRoomId() + " is already reserved from "
                        + reservation.getStay() + ".");
            }
        }
    }

    /**
     * Checks a reservation can still be cancelled.
     *
     * @param reservation the reservation
     * @throws BookingException if it is already cancelled or too close to check-in
     */
    public void checkCancellable(Reservation reservation) {
        if (!reservation.isActive()) {
            throw new BookingException("Reservation " + reservation.getBookingId() + " is already cancelled.");
        }
        checkCancellationWindow(reservation.getStay().checkIn());
    }

    /**
     * Checks the current time is before the cancellation cut-off for a check-in date.
     *
     * @param checkIn the check-in date
     * @throws BookingException if the cut-off has been reached
     */
    public void checkCancellationWindow(LocalDate checkIn) {
        LocalDateTime cutoff = cancellationCutoff(checkIn);
        // Reaching the cut-off exactly already counts as too late.
        if (!LocalDateTime.now(clock).isBefore(cutoff)) {
            throw new BookingException("Reservations cannot be cancelled within " + CANCELLATION_CUTOFF_HOURS
                    + " hours of check-in. The cut-off was " + cutoff.toLocalDate() + " "
                    + cutoff.toLocalTime() + ".");
        }
    }

    /**
     * Returns the last moment a booking can be cancelled.
     *
     * @param checkIn the check-in date
     * @return check-in date and time minus {@link #CANCELLATION_CUTOFF_HOURS}
     */
    public LocalDateTime cancellationCutoff(LocalDate checkIn) {
        return checkIn.atTime(STANDARD_CHECK_IN_TIME).minusHours(CANCELLATION_CUTOFF_HOURS);
    }
}
