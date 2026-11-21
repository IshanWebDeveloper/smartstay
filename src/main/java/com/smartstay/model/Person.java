package com.smartstay.model;

import java.util.Objects;

/**
 * A user of the system, with a full name and an identifier.
 */
public abstract class Person {

    private final String fullName;

    /**
     * Creates a person.
     *
     * @param fullName the full name; must not be blank
     */
    protected Person(String fullName) {
        this.fullName = requireText(fullName, "Full name");
    }

    /** @return the full name */
    public String getFullName() {
        return fullName;
    }

    /** @return the unique identifier */
    public abstract String getId();

    /** @return the role shown to users */
    public abstract String getRole();

    /**
     * Checks a value is present and not blank.
     *
     * @param value     the value to check
     * @param fieldName the name used in the error message
     * @return the trimmed value
     */
    protected static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " is required.");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
        return trimmed;
    }

    @Override
    public String toString() {
        return getRole() + " " + fullName + " (" + getId() + ")";
    }
}
