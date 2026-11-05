package com.smartstay.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.smartstay.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Checks {@link GuestValidator}.
 */
class GuestValidatorTest {

    private final GuestValidator validator = new GuestValidator();

    @ParameterizedTest(name = "\"{0}\" is accepted")
    @DisplayName("Valid NIC and passport numbers are accepted")
    @ValueSource(strings = {"200012345678", "991234567V", "991234567X", "P4826153", "N123456", "AB1234567"})
    void validIds(String id) {
        assertEquals(id, validator.checkNicOrPassport(id));
    }

    @Test
    @DisplayName("NIC is trimmed and upper-cased")
    void idNormalised() {
        assertEquals("991234567V", validator.checkNicOrPassport("  991234567v "));
    }

    @ParameterizedTest(name = "\"{0}\" is rejected")
    @DisplayName("Malformed NIC and passport numbers are rejected")
    @NullAndEmptySource
    @ValueSource(strings = {"12345", "1234567890123", "991234567VV", "P48-26153", "ABCDEFGHIJ"})
    void invalidIds(String id) {
        assertThrows(ValidationException.class, () -> validator.checkNicOrPassport(id));
    }

    @ParameterizedTest(name = "\"{0}\" is accepted")
    @DisplayName("Valid names are accepted")
    @ValueSource(strings = {"Nimal Perera", "Sarah O'Brien", "Anne-Marie Silva", "K. Fernando"})
    void validNames(String name) {
        assertEquals(name, validator.checkFullName(name));
    }

    @ParameterizedTest(name = "\"{0}\" is rejected")
    @DisplayName("Blank names and names with digits or symbols are rejected")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "Nimal2", "Sarah@Johnson"})
    void invalidNames(String name) {
        assertThrows(ValidationException.class, () -> validator.checkFullName(name));
    }

    @Test
    @DisplayName("Names over the length limit are rejected")
    void nameTooLong() {
        String longName = "A".repeat(GuestValidator.MAX_NAME_LENGTH + 1);
        assertThrows(ValidationException.class, () -> validator.checkFullName(longName));
    }

    @Test
    @DisplayName("Phone numbers drop spaces and hyphens")
    void phoneNormalised() {
        assertEquals("0771234567", validator.checkContactNumber("077 123-4567"));
        assertEquals("+94771234567", validator.checkContactNumber("+94 77 123 4567"));
    }

    @ParameterizedTest(name = "\"{0}\" is rejected")
    @DisplayName("Short, long or non-numeric phone numbers are rejected")
    @NullAndEmptySource
    @ValueSource(strings = {"12345", "1234567890123456", "07712345ab", "94+771234567"})
    void invalidPhones(String phone) {
        assertThrows(ValidationException.class, () -> validator.checkContactNumber(phone));
    }

    @Test
    @DisplayName("Email is trimmed and lower-cased")
    void emailNormalised() {
        assertEquals("nimal@example.com", validator.checkEmail(" Nimal@Example.com "));
    }

    @ParameterizedTest(name = "\"{0}\" is rejected")
    @DisplayName("Malformed emails are rejected")
    @NullAndEmptySource
    @ValueSource(strings = {"kamal@", "kamal", "@example.com", "kamal@example", "ka mal@example.com"})
    void invalidEmails(String email) {
        assertThrows(ValidationException.class, () -> validator.checkEmail(email));
    }
}
