package com.smartstay.util;

import java.util.Locale;

/**
 * Creates sequential booking IDs: SS-0001, SS-0002 and so on.
 */
public final class BookingIdGenerator {

    private static final String ID_FORMAT = "SS-%04d";

    private int lastNumber;

    /** @return the next booking ID */
    public String next() {
        lastNumber++;
        return String.format(Locale.ROOT, ID_FORMAT, lastNumber);
    }
}
