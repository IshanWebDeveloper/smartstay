package com.smartstay.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Checks {@link DateRange}: overlap rules and range validation.
 */
class DateRangeTest {

    private static final LocalDate DAY_10 = LocalDate.of(2026, 12, 10);
    private static final LocalDate DAY_12 = LocalDate.of(2026, 12, 12);
    private static final LocalDate DAY_13 = LocalDate.of(2026, 12, 13);
    private static final LocalDate DAY_15 = LocalDate.of(2026, 12, 15);

    private static DateRange range(LocalDate checkIn, LocalDate checkOut) {
        return new DateRange(checkIn, checkOut);
    }

    @Test
    @DisplayName("Identical ranges overlap")
    void identicalRangesOverlap() {
        assertTrue(range(DAY_10, DAY_13).overlaps(range(DAY_10, DAY_13)));
    }

    @Test
    @DisplayName("A range inside another overlaps, in both directions")
    void containedRangeOverlaps() {
        DateRange outer = range(DAY_10, DAY_15);
        DateRange inner = range(DAY_12, DAY_13);
        assertTrue(outer.overlaps(inner));
        assertTrue(inner.overlaps(outer));
    }

    @Test
    @DisplayName("Partially overlapping ranges overlap, in both directions")
    void partialOverlap() {
        DateRange first = range(DAY_10, DAY_13);
        DateRange second = range(DAY_12, DAY_15);
        assertTrue(first.overlaps(second));
        assertTrue(second.overlaps(first));
    }

    @Test
    @DisplayName("Back-to-back stays do not overlap")
    void backToBackDoesNotOverlap() {
        DateRange earlier = range(DAY_10, DAY_13);
        DateRange later = range(DAY_13, DAY_15);
        assertFalse(earlier.overlaps(later));
        assertFalse(later.overlaps(earlier));
    }

    @Test
    @DisplayName("Separate ranges do not overlap")
    void separateRangesDoNotOverlap() {
        assertFalse(range(DAY_10, DAY_12).overlaps(range(DAY_13, DAY_15)));
    }

    @Test
    @DisplayName("Check-out equal to check-in is rejected")
    void sameDayRangeRejected() {
        IllegalArgumentException error =
                assertThrows(IllegalArgumentException.class, () -> range(DAY_10, DAY_10));
        assertEquals("Check-out date must be after the check-in date.", error.getMessage());
    }

    @Test
    @DisplayName("Check-out before check-in is rejected")
    void reversedRangeRejected() {
        assertThrows(IllegalArgumentException.class, () -> range(DAY_12, DAY_10));
    }

    @Test
    @DisplayName("Missing dates are rejected")
    void nullDatesRejected() {
        assertThrows(NullPointerException.class, () -> range(null, DAY_12));
        assertThrows(NullPointerException.class, () -> range(DAY_10, null));
    }

    @Test
    @DisplayName("Nights is check-out minus check-in")
    void nightsCountsDaysBetween() {
        assertEquals(1, range(DAY_12, DAY_13).nights());
        assertEquals(5, range(DAY_10, DAY_15).nights());
    }

    @Test
    @DisplayName("Nights counts across a month and year end")
    void nightsAcrossYearEnd() {
        assertEquals(3, range(LocalDate.of(2026, 12, 30), LocalDate.of(2027, 1, 2)).nights());
    }
}
