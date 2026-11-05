package com.smartstay.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Checks {@link Invoice} and its {@link Invoice.Builder}.
 */
class InvoiceTest {

    private static final LocalDate CHECK_IN = LocalDate.of(2026, 12, 10);

    private static Reservation booking(RoomType type, BedType bed, int nights, int guests) {
        Guest guest = new Guest("199012345678", "Nimal Perera", "0771234567", "nimal@example.com");
        Room room = new Room("R900", type, bed, RoomStatus.AVAILABLE);
        return new Reservation("SS-0100", guest, room, new DateRange(CHECK_IN, CHECK_IN.plusDays(nights)), guests);
    }

    private static BigDecimal lkr(String amount) {
        return new BigDecimal(amount);
    }

    /** Deluxe, 6 nights, 4 guests. */
    private static Invoice.Builder deluxeInvoice() {
        return Invoice.builder()
                .reservation(booking(RoomType.DELUXE, BedType.QUEEN, 6, 4))
                .baseCharge(lkr("54000"))
                .extraGuestCharge(lkr("18000"))
                .discount(lkr("6480"))
                .tax(lkr("7862.40"))
                .totalCharge(lkr("73382.40"))
                .advanceDeducted(Reservation.BOOKING_ADVANCE);
    }

    @Test
    @DisplayName("Invoice holds all fields and the payable balance")
    void fullInvoice() {
        Invoice invoice = deluxeInvoice().build();

        assertEquals("SS-0100", invoice.getBookingId());
        assertEquals("Nimal Perera", invoice.getGuest().getFullName());
        assertEquals("R900", invoice.getRoomId());
        assertEquals(RoomType.DELUXE, invoice.getRoomType());
        assertEquals(BedType.QUEEN, invoice.getBedType());
        assertEquals(lkr("9000.00"), invoice.getDailyRate());
        assertTrue(invoice.isBreakfastIncluded());
        assertEquals(6, invoice.getNights());
        assertEquals(4, invoice.getNumberOfGuests());
        assertEquals(lkr("54000.00"), invoice.getBaseCharge());
        assertEquals(lkr("10000.00"), invoice.getAdvanceDeducted());
        assertFalse(invoice.isRefundDue());
        assertEquals(lkr("63382.40"), invoice.getAmountPayable());
        assertEquals(lkr("0.00"), invoice.getRefundDue());
    }

    @Test
    @DisplayName("Shows a refund when the advance exceeds the total")
    void refundDue() {
        Invoice invoice = Invoice.builder()
                .reservation(booking(RoomType.STANDARD, BedType.SINGLE, 1, 1))
                .baseCharge(lkr("6000"))
                .extraGuestCharge(BigDecimal.ZERO)
                .discount(BigDecimal.ZERO)
                .tax(lkr("600"))
                .totalCharge(lkr("6600"))
                .advanceDeducted(Reservation.BOOKING_ADVANCE)
                .build();

        assertFalse(invoice.isBreakfastIncluded());
        assertTrue(invoice.isRefundDue());
        assertEquals(lkr("3400.00"), invoice.getRefundDue());
        assertEquals(lkr("0.00"), invoice.getAmountPayable());
    }

    @Test
    @DisplayName("A total that does not add up is rejected")
    void inconsistentTotalRejected() {
        Invoice.Builder builder = deluxeInvoice().totalCharge(lkr("73382.41"));
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    @DisplayName("A missing amount is rejected")
    void missingAmountRejected() {
        Invoice.Builder builder = Invoice.builder()
                .reservation(booking(RoomType.STANDARD, BedType.SINGLE, 1, 1))
                .baseCharge(lkr("6000"));
        assertThrows(NullPointerException.class, builder::build);
    }

    @Test
    @DisplayName("A negative amount is rejected")
    void negativeAmountRejected() {
        assertThrows(IllegalArgumentException.class, () -> Invoice.builder().discount(lkr("-1")));
    }
}
