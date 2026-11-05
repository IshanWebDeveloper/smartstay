package com.smartstay.ui;

import com.smartstay.exception.SmartStayException;
import java.util.List;
import java.util.Objects;

/**
 * A numbered console menu. Subclasses supply the options; this class runs the loop
 * and turns exceptions into messages, so the user never sees a stack trace.
 */
public abstract class Menu {

    /**
     * One menu entry.
     *
     * @param label  the text shown
     * @param action what it does
     */
    protected record Option(String label, Runnable action) {
    }

    /** Reads the user's input. */
    protected final InputReader input;

    /** Writes the output. */
    protected final TablePrinter printer;

    /**
     * Creates a menu.
     *
     * @param input   reads the user's input
     * @param printer writes the output
     */
    protected Menu(InputReader input, TablePrinter printer) {
        this.input = Objects.requireNonNull(input, "Input reader is required.");
        this.printer = Objects.requireNonNull(printer, "Printer is required.");
    }

    /** @return the menu title */
    protected abstract String title();

    /** @return the options, numbered from 1 */
    protected abstract List<Option> options();

    /** @return the label for option 0 */
    protected String exitLabel() {
        return "Back";
    }

    /** Shows the menu and runs the chosen options until the user picks 0. */
    public void run() {
        List<Option> options = options();
        List<String> labels = options.stream().map(Option::label).toList();
        while (true) {
            printer.printMenu(title(), labels, exitLabel());
            int choice = input.readMenuChoice(options.size());
            if (choice == InputReader.BACK) {
                return;
            }
            runSafely(options.get(choice - 1).action());
        }
    }

    private void runSafely(Runnable action) {
        try {
            action.run();
        } catch (InputReader.BackRequested back) {
            printer.printMessage("Returning to the menu.");
        } catch (SmartStayException e) {
            printer.printError(e.getMessage());
        } catch (InputReader.InputClosed closed) {
            throw closed;
        } catch (RuntimeException unexpected) {
            printer.printError("Something went wrong. Please try again.");
        }
    }
}
