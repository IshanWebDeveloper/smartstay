package com.smartstay.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Checks the {@link RoomType} values.
 */
class RoomTypeTest {

    @Test
    @DisplayName("Values for Standard")
    void standardValues() {
        assertValues(RoomType.STANDARD, "6000.00", false, "1000.00", "0.10", 2);
    }

    @Test
    @DisplayName("Values for Deluxe")
    void deluxeValues() {
        assertValues(RoomType.DELUXE, "9000.00", true, "1500.00", "0.12", 2);
    }

    @Test
    @DisplayName("Values for Suite")
    void suiteValues() {
        assertValues(RoomType.SUITE, "14000.00", true, "2000.00", "0.15", 3);
    }

    @Test
    @DisplayName("Values for Family Suite")
    void familySuiteValues() {
        assertValues(RoomType.FAMILY_SUITE, "18000.00", true, "2500.00", "0.15", 4);
    }

    @Test
    @DisplayName("Maximum occupancy is capacity plus 2")
    void maxOccupancyIsCapacityPlusTwo() {
        assertEquals(4, RoomType.DELUXE.getMaxOccupancy());
        assertEquals(6, RoomType.FAMILY_SUITE.getMaxOccupancy());
    }

    @Test
    @DisplayName("Extra guests are only those above capacity")
    void countExtraGuests() {
        assertEquals(0, RoomType.SUITE.countExtraGuests(1));
        assertEquals(0, RoomType.SUITE.countExtraGuests(3));
        assertEquals(2, RoomType.DELUXE.countExtraGuests(4));
    }

    @Test
    @DisplayName("Display name is used for output")
    void displayName() {
        assertEquals("Family Suite", RoomType.FAMILY_SUITE.toString());
    }

    private static void assertValues(RoomType type, String rate, boolean breakfast,
                                     String extraCharge, String taxRate, int capacity) {
        assertEquals(new BigDecimal(rate), type.getDailyRate());
        if (breakfast) {
            assertTrue(type.hasComplimentaryBreakfast());
        } else {
            assertFalse(type.hasComplimentaryBreakfast());
        }
        assertEquals(new BigDecimal(extraCharge), type.getExtraGuestCharge());
        assertEquals(new BigDecimal(taxRate), type.getTaxRate());
        assertEquals(capacity, type.getCapacity());
    }
}
