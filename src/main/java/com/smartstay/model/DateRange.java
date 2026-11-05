package com.smartstay.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * An immutable stay, half-open: {@code [checkIn, checkOut)}.
 *
 * @param checkIn  the first night
 * @param checkOut the day the guest leaves; must be after {@code checkIn}
 */
public record DateRange(LocalDate checkIn, LocalDate checkOut) {

    /**
     * Validates the range.
     *
     * @throws IllegalArgumentException if check-out is not after check-in
     */
    public DateRange {
        Objects.requireNonNull(checkIn, "Check-in date is required.");
        Objects.requireNonNull(checkOut, "Check-out date is required.");
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Check-out date must be after the check-in date.");
        }
    }

    /** @return the number of nights */
    public int nights() {
        return Math.toIntExact(ChronoUnit.DAYS.between(checkIn, checkOut));
    }

    /**
     * Tells whether two stays share a night.
     *
     * @param other the stay to compare with
     * @return {@code true} if they overlap
     */
    public boolean overlaps(DateRange other) {
        Objects.requireNonNull(other, "Date range to compare with is required.");
        // Strict "before" on both sides, so back-to-back stays do not overlap.
        return this.checkIn.isBefore(other.checkOut) && other.checkIn.isBefore(this.checkOut);
    }

    @Override
    public String toString() {
        return checkIn + " to " + checkOut;
    }
}
