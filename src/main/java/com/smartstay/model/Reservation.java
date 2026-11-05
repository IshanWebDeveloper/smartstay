package com.smartstay.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * A guest's booking of one room for one stay. Only the status can change, by cancelling.
 */
public final class Reservation {

    /** Refundable advance charged on every booking, in LKR. */
    public static final BigDecimal BOOKING_ADVANCE = new BigDecimal("10000.00");

    private final String bookingId;
    private final Guest guest;
    private final Room room;
    private final DateRange stay;
    private final int numberOfGuests;
    private final BigDecimal advancePaid;
    private ReservationStatus status;

    /**
     * Creates a confirmed reservation with the booking advance recorded.
     *
     * @param bookingId      the booking ID, for example "SS-0003"
     * @param guest          the guest
     * @param room           the room
     * @param stay           the stay dates
     * @param numberOfGuests how many people will stay; at least 1
     */
    public Reservation(String bookingId, Guest guest, Room room, DateRange stay, int numberOfGuests) {
        Objects.requireNonNull(bookingId, "Booking ID is required.");
        if (bookingId.isBlank()) {
            throw new IllegalArgumentException("Booking ID must not be blank.");
        }
        if (numberOfGuests < 1) {
            throw new IllegalArgumentException("A reservation needs at least one guest.");
        }
        this.bookingId = bookingId.trim();
        this.guest = Objects.requireNonNull(guest, "Guest is required.");
        this.room = Objects.requireNonNull(room, "Room is required.");
        this.stay = Objects.requireNonNull(stay, "Stay dates are required.");
        this.numberOfGuests = numberOfGuests;
        this.advancePaid = BOOKING_ADVANCE;
        this.status = ReservationStatus.CONFIRMED;
    }

    /** @return the booking ID */
    public String getBookingId() {
        return bookingId;
    }

    /** @return the guest */
    public Guest getGuest() {
        return guest;
    }

    /** @return the room */
    public Room getRoom() {
        return room;
    }

    /** @return the stay dates */
    public DateRange getStay() {
        return stay;
    }

    /** @return the number of nights */
    public int getNights() {
        return stay.nights();
    }

    /** @return the number of guests */
    public int getNumberOfGuests() {
        return numberOfGuests;
    }

    /** @return the advance paid, in LKR */
    public BigDecimal getAdvancePaid() {
        return advancePaid;
    }

    /** @return the status */
    public ReservationStatus getStatus() {
        return status;
    }

    /** @return whether the booking has not been cancelled */
    public boolean isActive() {
        return status == ReservationStatus.CONFIRMED;
    }

    /**
     * Tells whether this booking holds its room on any night of the given dates.
     *
     * @param dates the dates to check
     * @return {@code true} if active and overlapping
     */
    public boolean blocksRoomDuring(DateRange dates) {
        return isActive() && stay.overlaps(dates);
    }

    /**
     * Cancels the booking.
     *
     * @throws IllegalStateException if already cancelled
     */
    public void cancel() {
        if (!isActive()) {
            throw new IllegalStateException("Reservation " + bookingId + " is already cancelled.");
        }
        status = ReservationStatus.CANCELLED;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof Reservation reservation && bookingId.equals(reservation.bookingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingId);
    }

    @Override
    public String toString() {
        return bookingId + ": " + guest.getFullName() + ", " + room.getRoomId() + ", " + stay
                + ", " + numberOfGuests + " guest(s), " + status;
    }
}
