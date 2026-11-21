package com.smartstay.model;

/**
 * A hotel administrator, signed in with a demo password.
 */
public final class Admin extends Person {

    private final String username;
    private final String password;

    /**
     * Creates an administrator.
     *
     * @param username the login name
     * @param fullName the full name
     * @param password the admin menu password
     */
    public Admin(String username, String fullName, String password) {
        super(fullName);
        this.username = requireText(username, "Username");
        this.password = requireText(password, "Password");
    }

    /** @return the login name */
    public String getUsername() {
        return username;
    }

    /**
     * Checks a password attempt.
     *
     * @param attempt the password typed; may be {@code null}
     * @return {@code true} if it matches
     */
    public boolean isPasswordCorrect(String attempt) {
        return password.equals(attempt);
    }

    @Override
    public String getId() {
        return username;
    }

    @Override
    public String getRole() {
        return "Admin";
    }
}
