package com.smartstay.pricing;

import com.smartstay.model.Invoice;
import com.smartstay.model.Reservation;
import com.smartstay.model.RoomType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Works out the final bill for a reservation, in five steps:
 * base charge, extra guests, discount, tax, then the advance deducted.
 */
public final class BillingCalculator {

    private final DiscountPolicy discountPolicy;

    /**
     * Creates a calculator.
     *
     * @param discountPolicy the policy deciding the discount
     */
    public BillingCalculator(DiscountPolicy discountPolicy) {
        this.discountPolicy = Objects.requireNonNull(discountPolicy, "Discount policy is required.");
    }

    /**
     * Calculates the bill for a reservation.
     *
     * @param reservation the reservation to bill
     * @return the invoice
     */
    public Invoice calculate(Reservation reservation) {
        Objects.requireNonNull(reservation, "Reservation is required.");
        RoomType type = reservation.getRoom().getType();
        int nights = reservation.getNights();

        BigDecimal baseCharge = type.getDailyRate().multiply(BigDecimal.valueOf(nights));

        // Charged per extra guest, per night.
        int extraGuests = type.countExtraGuests(reservation.getNumberOfGuests());
        BigDecimal extraGuestCharge = type.getExtraGuestCharge()
                .multiply(BigDecimal.valueOf((long) extraGuests * nights));

        // The discount applies to the base charge only, not to extra guest charges.
        BigDecimal discount = discountPolicy.discountFor(baseCharge, nights);

        BigDecimal taxable = baseCharge.add(extraGuestCharge).subtract(discount);
        BigDecimal tax = taxable.multiply(type.getTaxRate())
                .setScale(Invoice.MONEY_SCALE, RoundingMode.HALF_UP);
        BigDecimal totalCharge = taxable.add(tax);

        return Invoice.builder()
                .reservation(reservation)
                .baseCharge(baseCharge)
                .extraGuestCharge(extraGuestCharge)
                .discount(discount)
                .tax(tax)
                .totalCharge(totalCharge)
                .advanceDeducted(reservation.getAdvancePaid())
                .build();
    }
}
