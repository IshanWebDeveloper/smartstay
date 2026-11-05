package com.smartstay.pricing;

import com.smartstay.model.Invoice;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 12% off the base room charge for stays of 5 nights or more.
 */
public final class LongStayDiscount implements DiscountPolicy {

    /** Fewest nights that earn the discount. */
    public static final int LONG_STAY_NIGHTS = 5;

    /** Discount rate as a fraction of the base charge. */
    public static final BigDecimal LONG_STAY_DISCOUNT_RATE = new BigDecimal("0.12");

    @Override
    public BigDecimal discountFor(BigDecimal baseCharge, int nights) {
        Objects.requireNonNull(baseCharge, "Base charge is required.");
        if (nights < LONG_STAY_NIGHTS) {
            return BigDecimal.ZERO.setScale(Invoice.MONEY_SCALE);
        }
        return baseCharge.multiply(LONG_STAY_DISCOUNT_RATE)
                .setScale(Invoice.MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
