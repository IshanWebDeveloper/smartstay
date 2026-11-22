package com.smartstay.exception;

/**
 * Thrown when a room, guest or reservation does not exist.
 */
public class NotFoundException extends SmartStayException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     *
     * @param message what could not be found
     */
    public NotFoundException(String message) {
        super(message);
    }
}
