package com.smartstay.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Checks {@link BookingIdGenerator}.
 */
class BookingIdGeneratorTest {

    @Test
    @DisplayName("IDs are sequential and zero-padded, starting at SS-0001")
    void sequentialIds() {
        BookingIdGenerator generator = new BookingIdGenerator();
        assertEquals("SS-0001", generator.next());
        assertEquals("SS-0002", generator.next());
        assertEquals("SS-0003", generator.next());
    }
}
