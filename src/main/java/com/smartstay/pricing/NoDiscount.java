package com.smartstay.pricing;

import com.smartstay.model.Invoice;
import java.math.BigDecimal;

/**
 * A policy that never gives a discount.
 */
public final class NoDiscount implements DiscountPolicy {

    @Override
    public BigDecimal discountFor(BigDecimal baseCharge, int nights) {
        return BigDecimal.ZERO.setScale(Invoice.MONEY_SCALE);
    }
}
