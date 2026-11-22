package com.smartstay.ui;

import java.util.List;
import java.util.Objects;

/**
 * The top-level menu, leading to the guest and admin menus.
 */
public final class MainMenu extends Menu {

    /** Application name shown in the banner. */
    public static final String APP_NAME = "SmartStay Hotel Booking System";

    private final GuestMenu guestMenu;
    private final AdminMenu adminMenu;

    /**
     * Creates the menu.
     *
     * @param guestMenu the guest menu
     * @param adminMenu the admin menu
     * @param input     reads the user's input
     * @param printer   writes the output
     */
    public MainMenu(GuestMenu guestMenu, AdminMenu adminMenu, InputReader input, TablePrinter printer) {
        super(input, printer);
        this.guestMenu = Objects.requireNonNull(guestMenu, "Guest menu is required.");
        this.adminMenu = Objects.requireNonNull(adminMenu, "Admin menu is required.");
    }

    /** Shows the banner and runs the application until the user exits. */
    public void start() {
        printer.printBanner(APP_NAME);
        try {
            run();
            printer.printMessage("Goodbye.");
        } catch (InputReader.InputClosed closed) {
            printer.printMessage(System.lineSeparator() + "Input closed. Goodbye.");
        }
    }

    @Override
    protected String title() {
        return "Main Menu";
    }

    @Override
    protected List<Option> options() {
        return List.of(
                new Option("Guest menu", guestMenu::run),
                new Option("Admin menu", adminMenu::run));
    }

    @Override
    protected String exitLabel() {
        return "Exit";
    }
}
