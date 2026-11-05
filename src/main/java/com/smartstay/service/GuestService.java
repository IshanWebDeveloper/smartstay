package com.smartstay.service;

import com.smartstay.exception.NotFoundException;
import com.smartstay.exception.ValidationException;
import com.smartstay.model.Guest;
import com.smartstay.repository.Repository;
import com.smartstay.validation.GuestValidator;
import java.util.Objects;

/**
 * Registers and finds guests.
 */
public class GuestService {

    private final Repository<Guest, String> guests;
    private final GuestValidator validator;

    /**
     * Creates the service.
     *
     * @param guests    the guest store
     * @param validator the guest detail checks
     */
    public GuestService(Repository<Guest, String> guests, GuestValidator validator) {
        this.guests = Objects.requireNonNull(guests, "Guest repository is required.");
        this.validator = Objects.requireNonNull(validator, "Guest validator is required.");
    }

    /**
     * Registers a new guest.
     *
     * @param nicOrPassport the NIC or passport number
     * @param fullName      the full name
     * @param contactNumber the phone number
     * @param email         the email address
     * @return the new guest
     * @throws ValidationException if a value is invalid or the number is already registered
     */
    public Guest register(String nicOrPassport, String fullName, String contactNumber, String email) {
        String id = validator.checkNicOrPassport(nicOrPassport);
        if (guests.existsById(id)) {
            throw new ValidationException("A guest with number " + id + " is already registered.");
        }
        Guest guest = new Guest(id, validator.checkFullName(fullName),
                validator.checkContactNumber(contactNumber), validator.checkEmail(email));
        guests.save(guest);
        return guest;
    }

    /**
     * Tells whether a number is already registered.
     *
     * @param nicOrPassport the NIC or passport number
     * @return {@code true} if a guest has this number
     * @throws ValidationException if the number is malformed
     */
    public boolean isRegistered(String nicOrPassport) {
        return guests.existsById(validator.checkNicOrPassport(nicOrPassport));
    }

    /**
     * Finds a guest.
     *
     * @param nicOrPassport the NIC or passport number
     * @return the guest
     * @throws ValidationException if the number is malformed
     * @throws NotFoundException   if no guest has this number
     */
    public Guest findGuest(String nicOrPassport) {
        String id = validator.checkNicOrPassport(nicOrPassport);
        return guests.findById(id)
                .orElseThrow(() -> new NotFoundException("No guest is registered with number " + id + "."));
    }
}
