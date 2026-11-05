package com.smartstay.validation;

import com.smartstay.exception.ValidationException;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Checks the format of guest details. Each check returns the cleaned-up value.
 */
public class GuestValidator {

    private static final Pattern OLD_NIC = Pattern.compile("\\d{9}[VX]");
    private static final Pattern NEW_NIC = Pattern.compile("\\d{12}");
    private static final Pattern PASSPORT = Pattern.compile("[A-Z0-9]{6,9}");
    private static final Pattern NAME = Pattern.compile("[\\p{L} .'-]+");
    private static final Pattern PHONE = Pattern.compile("\\+?\\d{10,15}");
    private static final Pattern EMAIL = Pattern.compile("[^\\s@]+@[^\\s@]+\\.[^\\s@]+");

    /** Longest full name accepted. */
    public static final int MAX_NAME_LENGTH = 100;

    /**
     * Checks a NIC (old or new format) or passport number.
     *
     * @param value the number typed
     * @return the number trimmed and in upper case
     * @throws ValidationException if it matches none of the formats
     */
    public String checkNicOrPassport(String value) {
        String id = requireText(value, "NIC or passport number").toUpperCase(Locale.ROOT);
        if (OLD_NIC.matcher(id).matches() || NEW_NIC.matcher(id).matches()
                || PASSPORT.matcher(id).matches()) {
            return id;
        }
        throw new ValidationException("Enter a NIC (9 digits and V or X, or 12 digits) "
                + "or a passport number (6 to 9 letters or digits).");
    }

    /**
     * Checks a full name.
     *
     * @param value the name typed
     * @return the name trimmed
     * @throws ValidationException if it is blank, too long or has invalid characters
     */
    public String checkFullName(String value) {
        String name = requireText(value, "Full name");
        if (name.length() > MAX_NAME_LENGTH) {
            throw new ValidationException("Full name must be " + MAX_NAME_LENGTH + " characters or fewer.");
        }
        if (!NAME.matcher(name).matches()) {
            throw new ValidationException("Full name can only contain letters, spaces, full stops, apostrophes and hyphens.");
        }
        return name;
    }

    /**
     * Checks a phone number. Spaces and hyphens are ignored.
     *
     * @param value the number typed
     * @return the number with spaces and hyphens removed
     * @throws ValidationException if it is not 10 to 15 digits, with an optional leading '+'
     */
    public String checkContactNumber(String value) {
        String phone = requireText(value, "Contact number").replaceAll("[\\s-]", "");
        if (!PHONE.matcher(phone).matches()) {
            throw new ValidationException("Contact number must be 10 to 15 digits, optionally starting with '+'.");
        }
        return phone;
    }

    /**
     * Checks an email address.
     *
     * @param value the address typed
     * @return the address trimmed and in lower case
     * @throws ValidationException if it is not in the form name@domain.tld
     */
    public String checkEmail(String value) {
        String email = requireText(value, "Email").toLowerCase(Locale.ROOT);
        if (!EMAIL.matcher(email).matches()) {
            throw new ValidationException("Enter an email address like name@example.com.");
        }
        return email;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " is required.");
        }
        return value.trim();
    }
}
