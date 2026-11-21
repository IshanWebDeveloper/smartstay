package com.smartstay.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Checks the {@link Person} hierarchy: {@link Guest} and {@link Admin}.
 */
class PersonTest {

    private static Guest nimal() {
        return new Guest("199012345678", "Nimal Perera", "0771234567", "nimal@example.com");
    }

    @Test
    @DisplayName("Subclasses supply their own ID and role (polymorphism)")
    void rolesAndIds() {
        List<Person> people = List.of(nimal(), new Admin("admin", "Hotel Admin", "admin123"));
        assertEquals("Guest", people.get(0).getRole());
        assertEquals("199012345678", people.get(0).getId());
        assertEquals("Admin", people.get(1).getRole());
        assertEquals("admin", people.get(1).getId());
    }

    @Test
    @DisplayName("Guest values are trimmed and the ID is stored in upper case")
    void guestNormalisesId() {
        Guest guest = new Guest(" 991234567v ", " Kamal Silva ", "0711234567", "kamal@example.com");
        assertEquals("991234567V", guest.getNicOrPassport());
        assertEquals("Kamal Silva", guest.getFullName());
    }

    @Test
    @DisplayName("Guests with the same NIC are equal whatever the case")
    void guestEqualityByNic() {
        Guest lower = new Guest("p4826153", "Sarah Johnson", "0700000000", "sarah@example.com");
        Guest upper = new Guest("P4826153", "Sarah J.", "0711111111", "sj@example.com");
        assertEquals(lower, upper);
        assertEquals(lower.hashCode(), upper.hashCode());
        assertNotEquals(nimal(), upper);
    }

    @Test
    @DisplayName("Blank guest name is rejected")
    void blankNameRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Guest("199012345678", "   ", "0771234567", "nimal@example.com"));
    }

    @Test
    @DisplayName("Admin password check is exact")
    void adminPassword() {
        Admin admin = new Admin("admin", "Hotel Admin", "admin123");
        assertTrue(admin.isPasswordCorrect("admin123"));
        assertFalse(admin.isPasswordCorrect("ADMIN123"));
        assertFalse(admin.isPasswordCorrect(null));
    }
}
