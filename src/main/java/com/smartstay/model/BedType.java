package com.smartstay.model;

/**
 * The bed fitted in a room.
 */
public enum BedType {

    /** Single bed. */
    SINGLE("Single"),

    /** Double bed. */
    DOUBLE("Double"),

    /** Queen-size bed. */
    QUEEN("Queen"),

    /** King-size bed. */
    KING("King");

    private final String displayName;

    BedType(String displayName) {
        this.displayName = displayName;
    }

    /** @return the name shown to users */
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
