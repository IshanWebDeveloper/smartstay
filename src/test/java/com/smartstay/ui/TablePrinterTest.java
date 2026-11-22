package com.smartstay.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Checks {@link TablePrinter}.
 */
class TablePrinterTest {

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private final TablePrinter printer =
            new TablePrinter(new PrintStream(output, true, Charset.defaultCharset()));

    private List<String> printedLines() {
        return output.toString(Charset.defaultCharset()).lines().toList();
    }

    @Test
    @DisplayName("Table columns line up to the widest value")
    void tableAligned() {
        List<TablePrinter.Column<String>> columns = List.of(
                new TablePrinter.Column<>("Name", text -> text),
                new TablePrinter.Column<>("Size", text -> String.valueOf(text.length())));

        printer.printTable(columns, List.of("Al", "Barbara"));

        assertEquals(List.of(
                "",
                "  Name     Size",
                "  -------------",
                "  Al       2",
                "  Barbara  7"), printedLines());
    }

    @Test
    @DisplayName("Menus are numbered, with the exit option as 0")
    void menuNumbered() {
        printer.printMenu("Main", List.of("First", "Second"), "Exit");

        assertTrue(printedLines().containsAll(List.of("  1. First", "  2. Second", "  0. Exit")));
    }
}
