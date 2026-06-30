package com.assetmanager.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to print tabular data to the console with dynamic column widths.
 */
public class ConsoleTable {
    private final List<String> headers = new ArrayList<>();
    private final List<List<String>> rows = new ArrayList<>();

    public void setHeaders(String... headersArray) {
        headers.clear();
        for (String header : headersArray) {
            headers.add(header != null ? header : "");
        }
    }

    public void addRow(String... rowData) {
        List<String> row = new ArrayList<>();
        for (String cell : rowData) {
            row.add(cell != null ? cell : "N/A");
        }
        rows.add(row);
    }

    public void print() {
        if (headers.isEmpty()) {
            System.out.println("(Empty table)");
            return;
        }

        // Calculate max column widths
        int[] colWidths = new int[headers.size()];
        for (int i = 0; i < headers.size(); i++) {
            colWidths[i] = headers.get(i).length();
        }

        for (List<String> row : rows) {
            for (int i = 0; i < row.size(); i++) {
                if (i < colWidths.length) {
                    colWidths[i] = Math.max(colWidths[i], row.get(i).length());
                }
            }
        }

        // Format separator lines
        StringBuilder separatorBuilder = new StringBuilder("+");
        for (int width : colWidths) {
            separatorBuilder.append("-".repeat(width + 2)).append("+");
        }
        String separator = separatorBuilder.toString();

        // Print header
        System.out.println(separator);
        System.out.print("|");
        for (int i = 0; i < headers.size(); i++) {
            System.out.printf(" %-" + colWidths[i] + "s |", headers.get(i));
        }
        System.out.println();
        System.out.println(separator);

        // Print rows
        if (rows.isEmpty()) {
            System.out.print("|");
            int totalWidth = separator.length() - 4; // adjust for boundary spaces
            System.out.printf(" %-" + totalWidth + "s |", "No records found.");
            System.out.println();
        } else {
            for (List<String> row : rows) {
                System.out.print("|");
                for (int i = 0; i < row.size(); i++) {
                    if (i < colWidths.length) {
                        System.out.printf(" %-" + colWidths[i] + "s |", row.get(i));
                    }
                }
                System.out.println();
            }
        }
        System.out.println(separator);
    }
}
