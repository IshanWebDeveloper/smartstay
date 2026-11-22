package com.smartstay.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.smartstay.exception.ValidationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Checks {@link MoneyFormat} and {@link DateFormat}.
 */
class FormatTest {

    @Test
    @DisplayName("Money shows the currency, thousands separators and 2 decimals")
    void moneyFormat() {
        assertEquals("LKR 63,382.40", MoneyFormat.format(new BigDecimal("63382.40")));
        assertEquals("LKR 0.00", MoneyFormat.format(BigDecimal.ZERO));
        assertEquals("LKR 1,000,000.00", MoneyFormat.format(new BigDecimal("1000000")));
    }

    @Test
    @DisplayName("Money rounds half up")
    void moneyRoundsHalfUp() {
        assertEquals("LKR 1.01", MoneyFormat.format(new BigDecimal("1.005")));
    }

    @Test
    @DisplayName("Dates are written and read as yyyy-MM-dd")
    void dateRoundTrip() {
        LocalDate date = LocalDate.of(2026, 3, 7);
        assertEquals("2026-03-07", DateFormat.format(date));
        assertEquals(date, DateFormat.parse(" 2026-03-07 "));
    }

    @ParameterizedTest(name = "\"{0}\" is rejected")
    @DisplayName("Malformed and impossible dates are rejected")
    @NullAndEmptySource
    @ValueSource(strings = {"2026/13/40", "2026-13-01", "2026-02-30", "07-03-2026", "abc", "2026-3-7"})
    void invalidDatesRejected(String text) {
        ValidationException error = assertThrows(ValidationException.class, () -> DateFormat.parse(text));
        assertEquals("Enter a date in the format yyyy-MM-dd.", error.getMessage());
    }
}
