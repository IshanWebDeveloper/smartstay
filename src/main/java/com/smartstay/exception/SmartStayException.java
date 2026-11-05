package com.smartstay.exception;

/**
 * Base of all SmartStay exceptions. The message is written for the user.
 */
public abstract class SmartStayException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception.
     *
     * @param message a message the user can read
     */
    protected SmartStayException(String message) {
        super(message);
    }
}
