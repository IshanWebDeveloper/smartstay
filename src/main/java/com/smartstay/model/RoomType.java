package com.smartstay.model;

import java.math.BigDecimal;

/**
 * Room packages with their rate, breakfast, extra guest charge, tax rate and capacity.
 */
public enum RoomType {

    /** Standard room. */
    STANDARD("Standard", "6000.00", false, "1000.00", "0.10", 2),

    /** Deluxe room. */
    DELUXE("Deluxe", "9000.00", true, "1500.00", "0.12", 2),

    /** Suite. */
    SUITE("Suite", "14000.00", true, "2000.00", "0.15", 3),

    /** Family suite. */
    FAMILY_SUITE("Family Suite", "18000.00", true, "2500.00", "0.15", 4);

    /** Most guests allowed above capacity. */
    public static final int MAX_EXTRA_GUESTS = 2;

    private final String displayName;
    private final BigDecimal dailyRate;
    private final boolean complimentaryBreakfast;
    private final BigDecimal extraGuestCharge;
    private final BigDecimal taxRate;
    private final int capacity;

    // Strings keep each BigDecimal exact.
    RoomType(String displayName, String dailyRate, boolean complimentaryBreakfast,
             String extraGuestCharge, String taxRate, int capacity) {
        this.displayName = displayName;
        this.dailyRate = new BigDecimal(dailyRate);
        this.complimentaryBreakfast = complimentaryBreakfast;
        this.extraGuestCharge = new BigDecimal(extraGuestCharge);
        this.taxRate = new BigDecimal(taxRate);
        this.capacity = capacity;
    }

    /** @return the name shown to users */
    public String getDisplayName() {
        return displayName;
    }

    /** @return the nightly rate in LKR */
    public BigDecimal getDailyRate() {
        return dailyRate;
    }

    /** @return whether breakfast is included */
    public boolean hasComplimentaryBreakfast() {
        return complimentaryBreakfast;
    }

    /** @return the charge per extra guest per night in LKR */
    public BigDecimal getExtraGuestCharge() {
        return extraGuestCharge;
    }

    /** @return the tax rate as a fraction, for example 0.12 */
    public BigDecimal getTaxRate() {
        return taxRate;
    }

    /** @return the guests covered by the daily rate */
    public int getCapacity() {
        return capacity;
    }

    /** @return capacity plus {@link #MAX_EXTRA_GUESTS} */
    public int getMaxOccupancy() {
        return capacity + MAX_EXTRA_GUESTS;
    }

    /**
     * Returns how many guests are above capacity.
     *
     * @param numberOfGuests the guests on the booking
     * @return the extra guests, never negative
     */
    public int countExtraGuests(int numberOfGuests) {
        return Math.max(0, numberOfGuests - capacity);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
