package com.smartstay.pricing;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.smartstay.model.BedType;
import com.smartstay.model.DateRange;
import com.smartstay.model.Guest;
import com.smartstay.model.Invoice;
import com.smartstay.model.Reservation;
import com.smartstay.model.Room;
import com.smartstay.model.RoomStatus;
import com.smartstay.model.RoomType;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Checks {@link BillingCalculator} against hand-calculated bills.
 */
class BillingCalculatorTest {

    private static final LocalDate CHECK_IN = LocalDate.of(2026, 12, 10);

    private final BillingCalculator calculator = new BillingCalculator(new LongStayDiscount());

    private static Reservation booking(RoomType type, int nights, int guests) {
        Guest guest = new Guest("199012345678", "Nimal Perera", "0771234567", "nimal@example.com");
        Room room = new Room("R900", type, BedType.KING, RoomStatus.AVAILABLE);
        return new Reservation("SS-0100", guest, room,
                new DateRange(CHECK_IN, CHECK_IN.plusDays(nights)), guests);
    }

    private static BigDecimal lkr(String amount) {
        return new BigDecimal(amount);
    }

    @ParameterizedTest(name = "{0}, {1} nights, {2} guests: payable {8}")
    @DisplayName("Five-step bill matches hand-calculated values")
    @CsvSource({
        // type,         nights, guests, base,      extra,    discount, tax,      total,     payable
        "STANDARD,     2, 2, 12000.00,  0.00,     0.00,     1200.00,  13200.00,  3200.00",
        "DELUXE,       6, 4, 54000.00,  18000.00, 6480.00,  7862.40,  73382.40,  63382.40",
        "SUITE,        5, 3, 70000.00,  0.00,     8400.00,  9240.00,  70840.00,  60840.00",
        "SUITE,        4, 3, 56000.00,  0.00,     0.00,     8400.00,  64400.00,  54400.00",
        "FAMILY_SUITE, 7, 6, 126000.00, 35000.00, 15120.00, 21882.00, 167762.00, 157762.00"
    })
    void billMatchesExpected(RoomType type, int nights, int guests, String base, String extra,
                             String discount, String tax, String total, String payable) {
        Invoice invoice = calculator.calculate(booking(type, nights, guests));

        assertAll(
                () -> assertEquals(lkr(base), invoice.getBaseCharge(), "base"),
                () -> assertEquals(lkr(extra), invoice.getExtraGuestCharge(), "extra"),
                () -> assertEquals(lkr(discount), invoice.getDiscount(), "discount"),
                () -> assertEquals(lkr(tax), invoice.getTax(), "tax"),
                () -> assertEquals(lkr(total), invoice.getTotalCharge(), "total"),
                () -> assertEquals(lkr("10000.00"), invoice.getAdvanceDeducted(), "advance"),
                () -> assertFalse(invoice.isRefundDue(), "refund due"),
                () -> assertEquals(lkr(payable), invoice.getAmountPayable(), "payable"));
    }

    @Test
    @DisplayName("Advance above the total shows a refund")
    void advanceAboveTotalGivesRefund() {
        Invoice invoice = calculator.calculate(booking(RoomType.STANDARD, 1, 1));

        assertEquals(lkr("6000.00"), invoice.getBaseCharge());
        assertEquals(lkr("600.00"), invoice.getTax());
        assertEquals(lkr("6600.00"), invoice.getTotalCharge());
        assertTrue(invoice.isRefundDue());
        assertEquals(lkr("3400.00"), invoice.getRefundDue());
        assertEquals(lkr("0.00"), invoice.getAmountPayable());
    }

    @Test
    @DisplayName("The calculator uses whichever discount policy it is given")
    void usesInjectedPolicy() {
        BillingCalculator noDiscount = new BillingCalculator(new NoDiscount());
        Invoice invoice = noDiscount.calculate(booking(RoomType.DELUXE, 6, 4));

        assertEquals(lkr("0.00"), invoice.getDiscount());
        assertEquals(lkr("8640.00"), invoice.getTax());
        assertEquals(lkr("80640.00"), invoice.getTotalCharge());
    }
}
