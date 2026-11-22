package com.smartstay.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Formats money for display, like {@code LKR 63,382.40}.
 */
public final class MoneyFormat {

    private static final String PATTERN = "#,##0.00";
    private static final String CURRENCY_PREFIX = "LKR ";

    private MoneyFormat() {
    }

    /**
     * Formats an amount.
     *
     * @param amount the amount
     * @return the formatted amount
     */
    public static String format(BigDecimal amount) {
        // DecimalFormat is not thread-safe, so it is created per call rather than shared.
        DecimalFormat format = new DecimalFormat(PATTERN, DecimalFormatSymbols.getInstance(Locale.ROOT));
        format.setRoundingMode(RoundingMode.HALF_UP);
        return CURRENCY_PREFIX + format.format(amount);
    }
}
