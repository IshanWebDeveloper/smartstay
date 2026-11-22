package com.smartstay.service;

import com.smartstay.exception.BookingException;
import com.smartstay.exception.NotFoundException;
import com.smartstay.model.Invoice;
import com.smartstay.model.Reservation;
import com.smartstay.pricing.BillingCalculator;
import java.util.Objects;

/**
 * Creates invoices for reservations.
 */
public class InvoiceService {

    private final BookingService bookingService;
    private final BillingCalculator calculator;

    /**
     * Creates the service.
     *
     * @param bookingService finds reservations
     * @param calculator     works out the bill
     */
    public InvoiceService(BookingService bookingService, BillingCalculator calculator) {
        this.bookingService = Objects.requireNonNull(bookingService, "Booking service is required.");
        this.calculator = Objects.requireNonNull(calculator, "Billing calculator is required.");
    }

    /**
     * Creates the invoice for a reservation.
     *
     * @param bookingId the booking ID
     * @return the invoice
     * @throws NotFoundException if the reservation does not exist
     * @throws BookingException  if the reservation was cancelled
     */
    public Invoice generateInvoice(String bookingId) {
        Reservation reservation = bookingService.findByBookingId(bookingId);
        if (!reservation.isActive()) {
            throw new BookingException("Reservation " + reservation.getBookingId()
                    + " was cancelled and its advance refunded, so it has no invoice.");
        }
        return calculator.calculate(reservation);
    }
}
