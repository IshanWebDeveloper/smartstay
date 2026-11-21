package com.smartstay.pricing;

import java.math.BigDecimal;

/**
 * Decides the discount on a stay's base room charge.
 */
public interface DiscountPolicy {

    /**
     * Returns the discount for a stay.
     *
     * @param baseCharge the daily rate times nights
     * @param nights     the number of nights
     * @return the discount in LKR, scale 2, never negative
     */
    BigDecimal discountFor(BigDecimal baseCharge, int nights);
}
