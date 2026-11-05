package com.smartstay;

import java.io.PrintStream;

/**
 * Entry point for the SmartStay Hotel Booking System.
 *
 * <p>This class is the composition root: it will create the repositories,
 * services and menus and wire them together through their constructors, so
 * every dependency stays visible in code and on the class diagram. No
 * framework or dependency-injection container is used.</p>
 */
public final class SmartStayApp {

    /** Application name shown in the console banner. */
    public static final String APP_NAME = "SmartStay Hotel Booking System";

    private SmartStayApp() {
        // Utility entry point; not instantiated.
    }

    /**
     * Starts the console application.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        printBanner(System.out);
        // Wiring of repositories, services and the main menu is added in the next step.
    }

    /**
     * Prints the start-up banner.
     *
     * @param out the stream to write to
     */
    static void printBanner(PrintStream out) {
        String line = "=".repeat(APP_NAME.length() + 4);
        out.println(line);
        out.println("  " + APP_NAME);
        out.println(line);
    }
}
