package com.smartstay.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.smartstay.exception.BookingException;
import com.smartstay.exception.NotFoundException;
import com.smartstay.model.BedType;
import com.smartstay.model.DateRange;
import com.smartstay.model.Invoice;
import com.smartstay.model.RoomType;
import com.smartstay.pricing.BillingCalculator;
import com.smartstay.pricing.LongStayDiscount;
import com.smartstay.repository.InMemoryGuestRepository;
import com.smartstay.repository.InMemoryReservationRepository;
import com.smartstay.repository.InMemoryRoomRepository;
import com.smartstay.util.BookingIdGenerator;
import com.smartstay.validation.BookingRules;
import com.smartstay.validation.GuestValidator;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Checks {@link InvoiceService}.
 */
class InvoiceServiceTest {

    private static final ZoneId COLOMBO = ZoneId.of("Asia/Colombo");
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 5);

    private BookingService bookingService;
    private InvoiceService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(TODAY.atTime(12, 0).atZone(COLOMBO).toInstant(), COLOMBO);
        InMemoryReservationRepository reservations = new InMemoryReservationRepository();
        RoomService roomService = new RoomService(new InMemoryRoomRepository(), reservations, clock);
        GuestService guestService = new GuestService(new InMemoryGuestRepository(), new GuestValidator());
        bookingService = new BookingService(roomService, guestService, reservations,
                new BookingRules(clock), new BookingIdGenerator());
        service = new InvoiceService(bookingService, new BillingCalculator(new LongStayDiscount()));

        roomService.addRoom("R201", RoomType.DELUXE, BedType.QUEEN);
        guestService.register("199012345678", "Nimal Perera", "0771234567", "nimal@example.com");
        bookingService.createReservation("199012345678", "R201",
                new DateRange(TODAY.plusDays(5), TODAY.plusDays(11)), 4);
    }

    @Test
    @DisplayName("Creates the invoice for a reservation, ignoring ID case")
    void generateInvoice() {
        Invoice invoice = service.generateInvoice("ss-0001");

        assertEquals("SS-0001", invoice.getBookingId());
        assertEquals("R201", invoice.getRoomId());
        assertEquals(new BigDecimal("73382.40"), invoice.getTotalCharge());
        assertEquals(new BigDecimal("63382.40"), invoice.getAmountPayable());
    }

    @Test
    @DisplayName("A cancelled reservation has no invoice")
    void cancelledRejected() {
        bookingService.cancelReservation("SS-0001");

        assertThrows(BookingException.class, () -> service.generateInvoice("SS-0001"));
    }

    @Test
    @DisplayName("An unknown reservation is reported")
    void unknownRejected() {
        assertThrows(NotFoundException.class, () -> service.generateInvoice("SS-9999"));
    }
}
