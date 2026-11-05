package com.smartstay.exception;

/**
 * Thrown when a booking or cancellation breaks a business rule.
 */
public class BookingException extends SmartStayException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     *
     * @param message which rule was broken
     */
    public BookingException(String message) {
        super(message);
    }
}
