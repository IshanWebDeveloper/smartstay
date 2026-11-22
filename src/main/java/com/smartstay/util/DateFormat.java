package com.smartstay.util;

import com.smartstay.exception.ValidationException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Reads and writes dates as {@code yyyy-MM-dd}.
 */
public final class DateFormat {

    /** The date format shown to users. */
    public static final String DISPLAY_PATTERN = "yyyy-MM-dd";

    // Strict, so impossible dates such as 2026-02-30 are rejected rather than adjusted.
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private DateFormat() {
    }

    /**
     * Formats a date.
     *
     * @param date the date
     * @return the formatted date
     */
    public static String format(LocalDate date) {
        return date.format(FORMATTER);
    }

    /**
     * Parses a date.
     *
     * @param text the text typed
     * @return the date
     * @throws ValidationException if it is not a real date in the expected format
     */
    public static LocalDate parse(String text) {
        if (text == null) {
            throw new ValidationException("Enter a date in the format " + DISPLAY_PATTERN + ".");
        }
        try {
            return LocalDate.parse(text.trim(), FORMATTER);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Enter a date in the format " + DISPLAY_PATTERN + ".");
        }
    }
}
