package com.smartstay.exception;

/**
 * Thrown when input is missing or in the wrong format.
 */
public class ValidationException extends SmartStayException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     *
     * @param message what is wrong with the input
     */
    public ValidationException(String message) {
        super(message);
    }
}
