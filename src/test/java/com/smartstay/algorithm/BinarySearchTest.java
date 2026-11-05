package com.smartstay.algorithm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Checks {@link BinarySearch}.
 */
class BinarySearchTest {

    private static final List<String> IDS =
            List.of("SS-0001", "SS-0002", "SS-0003", "SS-0004", "SS-0005");

    private static int indexOf(List<String> sorted, String key) {
        return BinarySearch.indexOf(sorted, key, Function.identity(), Comparator.naturalOrder());
    }

    /** A booking-like item, searched by its ID. */
    private record Booking(String id, String guestName) {
    }

    @Test
    @DisplayName("Finds the first item")
    void findsFirst() {
        assertEquals(0, indexOf(IDS, "SS-0001"));
    }

    @Test
    @DisplayName("Finds the middle item")
    void findsMiddle() {
        assertEquals(2, indexOf(IDS, "SS-0003"));
    }

    @Test
    @DisplayName("Finds the last item")
    void findsLast() {
        assertEquals(4, indexOf(IDS, "SS-0005"));
    }

    @Test
    @DisplayName("Missing keys below, between and above the range are not found")
    void missingKeys() {
        List<String> gaps = List.of("SS-0002", "SS-0004", "SS-0006");
        assertEquals(BinarySearch.NOT_FOUND, indexOf(gaps, "SS-0001"));
        assertEquals(BinarySearch.NOT_FOUND, indexOf(gaps, "SS-0003"));
        assertEquals(BinarySearch.NOT_FOUND, indexOf(gaps, "SS-9999"));
    }

    @Test
    @DisplayName("Empty list finds nothing")
    void emptyList() {
        assertEquals(BinarySearch.NOT_FOUND, indexOf(List.of(), "SS-0001"));
    }

    @Test
    @DisplayName("Searches items by an extracted key")
    void findByKey() {
        List<Booking> bookings = List.of(
                new Booking("SS-0001", "Nimal Perera"),
                new Booking("SS-0002", "Sarah Johnson"),
                new Booking("SS-0003", "Kamal Silva"));

        assertEquals("Sarah Johnson", BinarySearch
                .find(bookings, "SS-0002", Booking::id, Comparator.naturalOrder())
                .orElseThrow().guestName());
        assertTrue(BinarySearch
                .find(bookings, "SS-9999", Booking::id, Comparator.naturalOrder())
                .isEmpty());
    }
}
