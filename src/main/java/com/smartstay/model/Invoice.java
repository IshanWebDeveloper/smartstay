package com.smartstay.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * An immutable invoice for one reservation. Build it with {@link Builder}.
 * Room details are copied at build time, so later room changes do not alter it.
 */
public final class Invoice {

    /** Decimal places for every amount. */
    public static final int MONEY_SCALE = 2;

    private final String bookingId;
    private final Guest guest;
    private final String roomId;
    private final RoomType roomType;
    private final BedType bedType;
    private final DateRange stay;
    private final int numberOfGuests;
    private final BigDecimal baseCharge;
    private final BigDecimal extraGuestCharge;
    private final BigDecimal discount;
    private final BigDecimal tax;
    private final BigDecimal totalCharge;
    private final BigDecimal advanceDeducted;

    private Invoice(Builder builder) {
        this.bookingId = builder.bookingId;
        this.guest = builder.guest;
        this.roomId = builder.roomId;
        this.roomType = builder.roomType;
        this.bedType = builder.bedType;
        this.stay = builder.stay;
        this.numberOfGuests = builder.numberOfGuests;
        this.baseCharge = builder.baseCharge;
        this.extraGuestCharge = builder.extraGuestCharge;
        this.discount = builder.discount;
        this.tax = builder.tax;
        this.totalCharge = builder.totalCharge;
        this.advanceDeducted = builder.advanceDeducted;
    }

    /** @return a new, empty builder */
    public static Builder builder() {
        return new Builder();
    }

    /** @return the booking ID */
    public String getBookingId() {
        return bookingId;
    }

    /** @return the guest */
    public Guest getGuest() {
        return guest;
    }

    /** @return the room ID */
    public String getRoomId() {
        return roomId;
    }

    /** @return the room type */
    public RoomType getRoomType() {
        return roomType;
    }

    /** @return the bed type */
    public BedType getBedType() {
        return bedType;
    }

    /** @return the nightly rate, in LKR */
    public BigDecimal getDailyRate() {
        return roomType.getDailyRate();
    }

    /** @return whether breakfast is included */
    public boolean isBreakfastIncluded() {
        return roomType.hasComplimentaryBreakfast();
    }

    /** @return the stay dates */
    public DateRange getStay() {
        return stay;
    }

    /** @return the number of nights */
    public int getNights() {
        return stay.nights();
    }

    /** @return the number of guests */
    public int getNumberOfGuests() {
        return numberOfGuests;
    }

    /** @return the base room charge */
    public BigDecimal getBaseCharge() {
        return baseCharge;
    }

    /** @return the extra guest charge */
    public BigDecimal getExtraGuestCharge() {
        return extraGuestCharge;
    }

    /** @return the discount, zero if none */
    public BigDecimal getDiscount() {
        return discount;
    }

    /** @return the tax */
    public BigDecimal getTax() {
        return tax;
    }

    /** @return the total before the advance is deducted */
    public BigDecimal getTotalCharge() {
        return totalCharge;
    }

    /** @return the advance deducted */
    public BigDecimal getAdvanceDeducted() {
        return advanceDeducted;
    }

    /** @return whether the advance exceeds the total */
    public boolean isRefundDue() {
        return balance().signum() < 0;
    }

    /** @return what the guest still owes, zero if a refund is due */
    public BigDecimal getAmountPayable() {
        return balance().max(zero());
    }

    /** @return what the hotel owes the guest, zero if the guest owes money */
    public BigDecimal getRefundDue() {
        return balance().negate().max(zero());
    }

    // Positive: the guest owes. Negative: the hotel owes.
    private BigDecimal balance() {
        return totalCharge.subtract(advanceDeducted);
    }

    private static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(MONEY_SCALE);
    }

    /**
     * Builds an {@link Invoice}. Every field is required; amounts are rounded to 2 places, HALF_UP.
     */
    public static final class Builder {

        private String bookingId;
        private Guest guest;
        private String roomId;
        private RoomType roomType;
        private BedType bedType;
        private DateRange stay;
        private int numberOfGuests;
        private BigDecimal baseCharge;
        private BigDecimal extraGuestCharge;
        private BigDecimal discount;
        private BigDecimal tax;
        private BigDecimal totalCharge;
        private BigDecimal advanceDeducted;

        private Builder() {
        }

        /**
         * Copies booking ID, guest, room, dates and guest count from a reservation.
         *
         * @param reservation the reservation being invoiced
         * @return this builder
         */
        public Builder reservation(Reservation reservation) {
            Objects.requireNonNull(reservation, "Reservation is required.");
            this.bookingId = reservation.getBookingId();
            this.guest = reservation.getGuest();
            this.stay = reservation.getStay();
            this.numberOfGuests = reservation.getNumberOfGuests();
            return room(reservation.getRoom());
        }

        /**
         * @param bookingId the booking ID
         * @return this builder
         */
        public Builder bookingId(String bookingId) {
            this.bookingId = bookingId;
            return this;
        }

        /**
         * @param guest the guest
         * @return this builder
         */
        public Builder guest(Guest guest) {
            this.guest = guest;
            return this;
        }

        /**
         * Copies the room ID, type and bed type.
         *
         * @param room the room
         * @return this builder
         */
        public Builder room(Room room) {
            Objects.requireNonNull(room, "Room is required.");
            this.roomId = room.getRoomId();
            this.roomType = room.getType();
            this.bedType = room.getBedType();
            return this;
        }

        /**
         * @param stay the stay dates
         * @return this builder
         */
        public Builder stay(DateRange stay) {
            this.stay = stay;
            return this;
        }

        /**
         * @param numberOfGuests the number of guests
         * @return this builder
         */
        public Builder numberOfGuests(int numberOfGuests) {
            this.numberOfGuests = numberOfGuests;
            return this;
        }

        /**
         * @param amount the base room charge
         * @return this builder
         */
        public Builder baseCharge(BigDecimal amount) {
            this.baseCharge = money(amount, "Base charge");
            return this;
        }

        /**
         * @param amount the extra guest charge
         * @return this builder
         */
        public Builder extraGuestCharge(BigDecimal amount) {
            this.extraGuestCharge = money(amount, "Extra guest charge");
            return this;
        }

        /**
         * @param amount the discount
         * @return this builder
         */
        public Builder discount(BigDecimal amount) {
            this.discount = money(amount, "Discount");
            return this;
        }

        /**
         * @param amount the tax
         * @return this builder
         */
        public Builder tax(BigDecimal amount) {
            this.tax = money(amount, "Tax");
            return this;
        }

        /**
         * @param amount the total before the advance
         * @return this builder
         */
        public Builder totalCharge(BigDecimal amount) {
            this.totalCharge = money(amount, "Total charge");
            return this;
        }

        /**
         * @param amount the advance deducted
         * @return this builder
         */
        public Builder advanceDeducted(BigDecimal amount) {
            this.advanceDeducted = money(amount, "Advance deducted");
            return this;
        }

        /**
         * Creates the invoice.
         *
         * @return the invoice
         * @throws NullPointerException     if a field is missing
         * @throws IllegalArgumentException if there are no guests or the total does not add up
         */
        public Invoice build() {
            Objects.requireNonNull(bookingId, "Booking ID is required.");
            Objects.requireNonNull(guest, "Guest is required.");
            Objects.requireNonNull(roomId, "Room is required.");
            Objects.requireNonNull(stay, "Stay dates are required.");
            Objects.requireNonNull(baseCharge, "Base charge is required.");
            Objects.requireNonNull(extraGuestCharge, "Extra guest charge is required.");
            Objects.requireNonNull(discount, "Discount is required.");
            Objects.requireNonNull(tax, "Tax is required.");
            Objects.requireNonNull(totalCharge, "Total charge is required.");
            Objects.requireNonNull(advanceDeducted, "Advance deducted is required.");
            if (numberOfGuests < 1) {
                throw new IllegalArgumentException("An invoice needs at least one guest.");
            }
            BigDecimal expectedTotal = baseCharge.add(extraGuestCharge).subtract(discount).add(tax);
            if (expectedTotal.compareTo(totalCharge) != 0) {
                throw new IllegalArgumentException("Total charge " + totalCharge
                        + " does not equal base + extra - discount + tax (" + expectedTotal + ").");
            }
            return new Invoice(this);
        }

        private static BigDecimal money(BigDecimal amount, String fieldName) {
            Objects.requireNonNull(amount, fieldName + " is required.");
            if (amount.signum() < 0) {
                throw new IllegalArgumentException(fieldName + " must not be negative.");
            }
            return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        }
    }
}
