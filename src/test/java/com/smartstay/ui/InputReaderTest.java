package com.smartstay.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.smartstay.exception.ValidationException;
import com.smartstay.model.RoomType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Checks {@link InputReader} with scripted input.
 */
class InputReaderTest {

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    private InputReader readerFor(String... lines) {
        byte[] script = (String.join(System.lineSeparator(), lines) + System.lineSeparator())
                .getBytes(Charset.defaultCharset());
        TablePrinter printer = new TablePrinter(new PrintStream(output, true, Charset.defaultCharset()));
        return new InputReader(new ByteArrayInputStream(script), printer);
    }

    private String printed() {
        return output.toString(Charset.defaultCharset());
    }

    @Test
    @DisplayName("A menu choice re-prompts on text and out-of-range numbers")
    void menuChoiceReprompts() {
        assertEquals(2, readerFor("abc", "9", "-1", "2").readMenuChoice(3));
        assertTrue(printed().contains("Enter a number from 0 to 3."));
    }

    @Test
    @DisplayName("0 is a valid menu choice")
    void menuChoiceZero() {
        assertEquals(InputReader.BACK, readerFor("0").readMenuChoice(3));
    }

    @Test
    @DisplayName("read re-prompts with the parser's message until it succeeds")
    void readReprompts() {
        InputReader reader = readerFor("bad", "good");

        String value = reader.read("Value", text -> {
            if (text.equals("bad")) {
                throw new ValidationException("That is bad.");
            }
            return text;
        });

        assertEquals("good", value);
        assertTrue(printed().contains("Error: That is bad."));
    }

    @Test
    @DisplayName("Entering 0 at a prompt goes back")
    void zeroGoesBack() {
        InputReader reader = readerFor("0");
        assertThrows(InputReader.BackRequested.class, () -> reader.readText("Value"));
    }

    @Test
    @DisplayName("A date re-prompts showing the expected format")
    void dateReprompts() {
        assertEquals(LocalDate.of(2026, 12, 10), readerFor("2026/13/40", "2026-12-10").readDate("Date"));
        assertTrue(printed().contains("Enter a date in the format yyyy-MM-dd."));
    }

    @Test
    @DisplayName("A positive number rejects 0, negatives and text")
    void positiveNumberReprompts() {
        assertEquals(2, readerFor("0", "-3", "two", "2").readPositiveNumber("Guests"));
        assertTrue(printed().contains("Enter a whole number of 1 or more."));
    }

    @Test
    @DisplayName("An option is chosen by number, re-prompting on a bad one")
    void optionChosenByNumber() {
        RoomType type = readerFor("9", "2").readOption("Room type", List.of(RoomType.values()));

        assertEquals(RoomType.DELUXE, type);
        assertTrue(printed().contains("2. Deluxe"));
    }

    @Test
    @DisplayName("Entering 0 at an option list goes back")
    void optionZeroGoesBack() {
        InputReader reader = readerFor("0");
        List<RoomType> types = List.of(RoomType.values());
        assertThrows(InputReader.BackRequested.class, () -> reader.readOption("Room type", types));
    }

    @Test
    @DisplayName("Confirm accepts Y and N in any case and re-prompts on anything else")
    void confirm() {
        assertTrue(readerFor("y").confirm("Sure?"));
        assertFalse(readerFor("maybe", "N").confirm("Sure?"));
        assertTrue(printed().contains("Enter Y or N."));
    }

    @Test
    @DisplayName("Running out of input is reported, not a null")
    void inputClosed() {
        TablePrinter printer = new TablePrinter(new PrintStream(output, true, Charset.defaultCharset()));
        InputReader reader = new InputReader(new ByteArrayInputStream(new byte[0]), printer);
        assertThrows(InputReader.InputClosed.class, () -> reader.readLine("Value"));
    }
}
