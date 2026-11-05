package com.smartstay.ui;

import com.smartstay.exception.SmartStayException;
import com.smartstay.util.DateFormat;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.Function;

/**
 * Reads console input, re-prompting until the input is valid.
 */
public final class InputReader {

    /** The menu number that goes back. */
    public static final int BACK = 0;

    private static final String BACK_INPUT = String.valueOf(BACK);

    /** Thrown when the user enters 0 at a prompt; the menu returns to its options. */
    static final class BackRequested extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    /** Thrown when no more input can be read, such as after Ctrl+Z; the application ends. */
    static final class InputClosed extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    private final BufferedReader reader;
    private final TablePrinter printer;

    /**
     * Creates a reader.
     *
     * @param in      where to read from
     * @param printer shows prompts and errors
     */
    public InputReader(InputStream in, TablePrinter printer) {
        Objects.requireNonNull(in, "Input stream is required.");
        this.reader = new BufferedReader(new InputStreamReader(in, Charset.defaultCharset()));
        this.printer = Objects.requireNonNull(printer, "Printer is required.");
    }

    /**
     * Creates a reader for the console.
     *
     * @param printer shows prompts and errors
     * @return the reader
     */
    public static InputReader forConsole(TablePrinter printer) {
        return new InputReader(System.in, printer);
    }

    /**
     * Reads one line, where 0 has no special meaning.
     *
     * @param prompt the prompt
     * @return the line, trimmed
     */
    public String readLine(String prompt) {
        printer.printPrompt(prompt + ": ");
        try {
            String line = reader.readLine();
            if (line == null) {
                throw new InputClosed();
            }
            return line.trim();
        } catch (IOException e) {
            throw new InputClosed();
        }
    }

    /**
     * Reads a value, re-prompting while the parser rejects it. Entering 0 goes back.
     *
     * @param prompt the prompt
     * @param parser converts the text, or throws an exception with a message for the user
     * @param <T>    the value type
     * @return the value
     */
    public <T> T read(String prompt, Function<String, T> parser) {
        while (true) {
            String line = readLine(prompt + " [" + BACK_INPUT + " = Back]");
            if (BACK_INPUT.equals(line)) {
                throw new BackRequested();
            }
            try {
                return parser.apply(line);
            } catch (SmartStayException e) {
                printer.printError(e.getMessage());
            }
        }
    }

    /**
     * Reads text as typed. Entering 0 goes back.
     *
     * @param prompt the prompt
     * @return the text
     */
    public String readText(String prompt) {
        return read(prompt, Function.identity());
    }

    /**
     * Reads a date. Entering 0 goes back.
     *
     * @param prompt the prompt
     * @return the date
     */
    public LocalDate readDate(String prompt) {
        return read(prompt + " (" + DateFormat.DISPLAY_PATTERN + ")", DateFormat::parse);
    }

    /**
     * Reads a menu choice.
     *
     * @param highestOption the highest number on the menu
     * @return a number from 0 to {@code highestOption}
     */
    public int readMenuChoice(int highestOption) {
        while (true) {
            OptionalInt choice = parseNumber(readLine("Choose an option"));
            if (choice.isPresent() && choice.getAsInt() >= BACK && choice.getAsInt() <= highestOption) {
                return choice.getAsInt();
            }
            printer.printError("Enter a number from " + BACK + " to " + highestOption + ".");
        }
    }

    /**
     * Reads a whole number of at least 1. Here 0 is rejected and does not go back.
     *
     * @param prompt the prompt
     * @return the number
     */
    public int readPositiveNumber(String prompt) {
        while (true) {
            OptionalInt number = parseNumber(readLine(prompt));
            if (number.isPresent() && number.getAsInt() >= 1) {
                return number.getAsInt();
            }
            printer.printError("Enter a whole number of 1 or more.");
        }
    }

    /**
     * Shows a numbered list and reads a choice from it. Entering 0 goes back.
     *
     * @param title   the list title
     * @param options the options, shown with {@code toString}
     * @param <E>     the option type
     * @return the chosen option
     */
    public <E> E readOption(String title, List<E> options) {
        printer.printMenu(title, options.stream().map(Object::toString).toList(), "Back");
        int choice = readMenuChoice(options.size());
        if (choice == BACK) {
            throw new BackRequested();
        }
        return options.get(choice - 1);
    }

    /**
     * Asks a yes or no question.
     *
     * @param question the question
     * @return {@code true} for yes
     */
    public boolean confirm(String question) {
        while (true) {
            String answer = readLine(question + " (Y/N)").toUpperCase(Locale.ROOT);
            if (answer.equals("Y") || answer.equals("YES")) {
                return true;
            }
            if (answer.equals("N") || answer.equals("NO")) {
                return false;
            }
            printer.printError("Enter Y or N.");
        }
    }

    private static OptionalInt parseNumber(String text) {
        try {
            return OptionalInt.of(Integer.parseInt(text));
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }
}
