package com.smartstay.pricing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Checks {@link LongStayDiscount} and {@link NoDiscount}.
 */
class LongStayDiscountTest {

    private final DiscountPolicy longStay = new LongStayDiscount();

    @Test
    @DisplayName("4 nights get no discount")
    void fourNightsNoDiscount() {
        assertEquals(new BigDecimal("0.00"), longStay.discountFor(new BigDecimal("56000"), 4));
    }

    @Test
    @DisplayName("5 nights get 12% of the base charge")
    void fiveNightsTwelvePercent() {
        assertEquals(new BigDecimal("8400.00"), longStay.discountFor(new BigDecimal("70000"), 5));
    }

    @Test
    @DisplayName("Longer stays also get 12%")
    void longerStayTwelvePercent() {
        assertEquals(new BigDecimal("15120.00"), longStay.discountFor(new BigDecimal("126000"), 7));
    }

    @Test
    @DisplayName("No discount policy always returns zero")
    void noDiscountIsZero() {
        assertEquals(new BigDecimal("0.00"), new NoDiscount().discountFor(new BigDecimal("126000"), 7));
    }
}
