package com.smartstay.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.smartstay.exception.NotFoundException;
import com.smartstay.exception.ValidationException;
import com.smartstay.model.Guest;
import com.smartstay.repository.InMemoryGuestRepository;
import com.smartstay.validation.GuestValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Checks {@link GuestService}.
 */
class GuestServiceTest {

    private final GuestService service = new GuestService(new InMemoryGuestRepository(), new GuestValidator());

    private Guest registerNimal() {
        return service.register("199012345678", "Nimal Perera", "0771234567", "nimal@example.com");
    }

    @Test
    @DisplayName("Registering stores the guest with cleaned-up details")
    void register() {
        Guest guest = service.register(" 991234567v ", " Kamal Silva ", "077 123 4567", "Kamal@Example.com");

        assertEquals("991234567V", guest.getNicOrPassport());
        assertEquals("Kamal Silva", guest.getFullName());
        assertEquals("0771234567", guest.getContactNumber());
        assertEquals("kamal@example.com", guest.getEmail());
        assertEquals(guest, service.findGuest("991234567V"));
    }

    @Test
    @DisplayName("A duplicate number is rejected")
    void duplicateRejected() {
        registerNimal();
        assertThrows(ValidationException.class, this::registerNimal);
    }

    @Test
    @DisplayName("A duplicate is found whatever the case of the number")
    void duplicateIgnoresCase() {
        service.register("P4826153", "Sarah Johnson", "0700000000", "sarah@example.com");
        assertThrows(ValidationException.class,
                () -> service.register("p4826153", "Sarah Johnson", "0700000000", "sarah@example.com"));
    }

    @Test
    @DisplayName("Invalid details are rejected and nothing is stored")
    void invalidDetailsRejected() {
        assertThrows(ValidationException.class,
                () -> service.register("12345", "Kamal Silva", "0771234567", "kamal@example.com"));
        assertThrows(ValidationException.class,
                () -> service.register("200012345678", "", "0771234567", "kamal@example.com"));
        assertThrows(ValidationException.class,
                () -> service.register("200012345678", "Kamal Silva", "12345", "kamal@example.com"));
        assertThrows(ValidationException.class,
                () -> service.register("200012345678", "Kamal Silva", "0771234567", "kamal@"));

        assertFalse(service.isRegistered("200012345678"));
    }

    @Test
    @DisplayName("Tells whether a number is registered")
    void isRegistered() {
        registerNimal();
        assertTrue(service.isRegistered("199012345678"));
        assertFalse(service.isRegistered("188888888V"));
    }

    @Test
    @DisplayName("An unregistered number is reported")
    void unknownGuest() {
        assertThrows(NotFoundException.class, () -> service.findGuest("188888888V"));
    }
}
