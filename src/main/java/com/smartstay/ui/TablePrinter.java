package com.smartstay.ui;

import com.smartstay.model.Guest;
import com.smartstay.model.Invoice;
import com.smartstay.model.Reservation;
import com.smartstay.model.Room;
import com.smartstay.util.DateFormat;
import com.smartstay.util.MoneyFormat;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Writes all console output: headings, menus, messages, aligned tables and invoices.
 */
public final class TablePrinter {

    /**
     * One column of a table.
     *
     * @param header the column heading
     * @param value  extracts the cell text from a row
     * @param <T>    the row type
     */
    public record Column<T>(String header, Function<T, String> value) {
    }

    private static final String INDENT = "  ";
    private static final String COLUMN_GAP = "  ";
    private static final String DETAIL_FORMAT = INDENT + "%-22s %s";

    private static final List<Column<Room>> ROOM_COLUMNS = List.of(
            new Column<>("Room ID", Room::getRoomId),
            new Column<>("Type", room -> room.getType().getDisplayName()),
            new Column<>("Bed", room -> room.getBedType().getDisplayName()),
            new Column<>("Daily Rate", room -> MoneyFormat.format(room.getDailyRate())),
            new Column<>("Breakfast", room -> room.getType().hasComplimentaryBreakfast() ? "Yes" : "No"),
            new Column<>("Capacity", room -> String.valueOf(room.getType().getCapacity())),
            new Column<>("Status", room -> room.getStatus().getDisplayName()));

    private static final List<Column<Reservation>> RESERVATION_COLUMNS = List.of(
            new Column<>("Booking ID", Reservation::getBookingId),
            new Column<>("Guest", reservation -> reservation.getGuest().getFullName()),
            new Column<>("Room", reservation -> reservation.getRoom().getRoomId()),
            new Column<>("Check-in", reservation -> DateFormat.format(reservation.getStay().checkIn())),
            new Column<>("Check-out", reservation -> DateFormat.format(reservation.getStay().checkOut())),
            new Column<>("Nights", reservation -> String.valueOf(reservation.getNights())),
            new Column<>("Guests", reservation -> String.valueOf(reservation.getNumberOfGuests())),
            new Column<>("Status", reservation -> reservation.getStatus().getDisplayName()));

    private final PrintStream out;

    /**
     * Creates a printer.
     *
     * @param out where to write
     */
    public TablePrinter(PrintStream out) {
        this.out = Objects.requireNonNull(out, "Output stream is required.");
    }

    /** @return a printer that writes to the console */
    public static TablePrinter forConsole() {
        return new TablePrinter(System.out);
    }

    /**
     * Prints the start-up banner.
     *
     * @param title the application name
     */
    public void printBanner(String title) {
        String line = "=".repeat(title.length() + INDENT.length() * 2);
        out.println(line);
        out.println(INDENT + title);
        out.println(line);
    }

    /**
     * Prints an underlined heading.
     *
     * @param title the heading
     */
    public void printHeading(String title) {
        out.println();
        out.println(title);
        out.println("-".repeat(title.length()));
    }

    /**
     * Prints a numbered menu, with the exit option as 0.
     *
     * @param title     the menu title
     * @param labels    the options, numbered from 1
     * @param exitLabel the label for option 0
     */
    public void printMenu(String title, List<String> labels, String exitLabel) {
        printHeading(title);
        for (int i = 0; i < labels.size(); i++) {
            out.println(INDENT + (i + 1) + ". " + labels.get(i));
        }
        out.println(INDENT + InputReader.BACK + ". " + exitLabel);
    }

    /**
     * Prints a message.
     *
     * @param message the message
     */
    public void printMessage(String message) {
        out.println(message);
    }

    /**
     * Prints an error message.
     *
     * @param message what went wrong
     */
    public void printError(String message) {
        out.println("Error: " + message);
    }

    /**
     * Prints a prompt, leaving the cursor on the same line.
     *
     * @param prompt the prompt
     */
    public void printPrompt(String prompt) {
        out.print(prompt);
        out.flush();
    }

    /**
     * Prints rows as an aligned table.
     *
     * @param columns the columns
     * @param rows    the rows
     * @param <T>     the row type
     */
    public <T> void printTable(List<Column<T>> columns, List<T> rows) {
        List<List<String>> cells = new ArrayList<>();
        cells.add(columns.stream().map(Column::header).toList());
        for (T row : rows) {
            cells.add(columns.stream().map(column -> column.value().apply(row)).toList());
        }
        int[] widths = columnWidths(cells, columns.size());

        out.println();
        printRow(cells.get(0), widths);
        out.println(INDENT + separator(widths));
        for (List<String> row : cells.subList(1, cells.size())) {
            printRow(row, widths);
        }
    }

    /**
     * Prints rooms as a table.
     *
     * @param rooms the rooms
     */
    public void printRooms(List<Room> rooms) {
        printTable(ROOM_COLUMNS, rooms);
    }

    /**
     * Prints reservations as a table.
     *
     * @param reservations the reservations
     */
    public void printReservations(List<Reservation> reservations) {
        printTable(RESERVATION_COLUMNS, reservations);
    }

    /**
     * Prints an invoice with all its details.
     *
     * @param invoice the invoice
     */
    public void printInvoice(Invoice invoice) {
        Guest guest = invoice.getGuest();
        printHeading("Invoice for " + invoice.getBookingId());
        printDetail("Guest", guest.getFullName());
        printDetail("NIC / passport", guest.getNicOrPassport());
        printDetail("Contact number", guest.getContactNumber());
        printDetail("Email", guest.getEmail());
        printDetail("Room ID", invoice.getRoomId());
        printDetail("Room type", invoice.getRoomType().getDisplayName());
        printDetail("Bed type", invoice.getBedType().getDisplayName());
        printDetail("Daily rate", MoneyFormat.format(invoice.getDailyRate()));
        printDetail("Breakfast", invoice.isBreakfastIncluded() ? "Included" : "Not included");
        printDetail("Check-in", DateFormat.format(invoice.getStay().checkIn()));
        printDetail("Check-out", DateFormat.format(invoice.getStay().checkOut()));
        printDetail("Nights", String.valueOf(invoice.getNights()));
        printDetail("Guests", String.valueOf(invoice.getNumberOfGuests()));
        out.println();
        printAmount("Base room charge", invoice.getBaseCharge());
        printAmount("Extra guest charges", invoice.getExtraGuestCharge());
        printAmount("Discount", invoice.getDiscount());
        printAmount("Tax", invoice.getTax());
        printAmount("Total", invoice.getTotalCharge());
        printAmount("Advance deducted", invoice.getAdvanceDeducted());
        if (invoice.isRefundDue()) {
            printAmount("Refund due", invoice.getRefundDue());
        } else {
            printAmount("Amount payable", invoice.getAmountPayable());
        }
    }

    private void printDetail(String label, String value) {
        out.println(String.format(DETAIL_FORMAT, label, value));
    }

    private void printAmount(String label, BigDecimal amount) {
        printDetail(label, MoneyFormat.format(amount));
    }

    private void printRow(List<String> row, int[] widths) {
        StringBuilder line = new StringBuilder(INDENT);
        for (int i = 0; i < row.size(); i++) {
            line.append(String.format("%-" + widths[i] + "s", row.get(i))).append(COLUMN_GAP);
        }
        out.println(line.toString().stripTrailing());
    }

    private static int[] columnWidths(List<List<String>> cells, int columnCount) {
        int[] widths = new int[columnCount];
        for (List<String> row : cells) {
            for (int i = 0; i < columnCount; i++) {
                widths[i] = Math.max(widths[i], row.get(i).length());
            }
        }
        return widths;
    }

    private static String separator(int[] widths) {
        int total = 0;
        for (int width : widths) {
            total += width;
        }
        return "-".repeat(total + COLUMN_GAP.length() * (widths.length - 1));
    }
}
