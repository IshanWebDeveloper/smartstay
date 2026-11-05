package com.smartstay.model;

import java.util.Locale;
import java.util.Objects;

/**
 * A registered guest, identified by NIC or passport number.
 */
public final class Guest extends Person {

    private final String nicOrPassport;
    private final String contactNumber;
    private final String email;

    /**
     * Creates a guest.
     *
     * @param nicOrPassport the NIC or passport number, stored in upper case
     * @param fullName      the full name
     * @param contactNumber the phone number
     * @param email         the email address
     */
    public Guest(String nicOrPassport, String fullName, String contactNumber, String email) {
        super(fullName);
        this.nicOrPassport = requireText(nicOrPassport, "NIC or passport number").toUpperCase(Locale.ROOT);
        this.contactNumber = requireText(contactNumber, "Contact number");
        this.email = requireText(email, "Email");
    }

    /** @return the NIC or passport number */
    public String getNicOrPassport() {
        return nicOrPassport;
    }

    /** @return the phone number */
    public String getContactNumber() {
        return contactNumber;
    }

    /** @return the email address */
    public String getEmail() {
        return email;
    }

    @Override
    public String getId() {
        return nicOrPassport;
    }

    @Override
    public String getRole() {
        return "Guest";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof Guest guest && nicOrPassport.equals(guest.nicOrPassport);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nicOrPassport);
    }
}
