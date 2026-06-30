package com.assetmanager.util;

import com.assetmanager.exception.ValidationException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Utility class for parsing and validating console inputs.
 */
public class InputValidator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@(.+)$"
    );

    /**
     * Checks if the email matches a basic standard email regex.
     */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Parses a date string in YYYY-MM-DD format.
     */
    public static LocalDate parseDate(String dateStr) throws ValidationException {
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid date format. Please enter as YYYY-MM-DD (e.g., 2026-06-30).");
        }
    }

    /**
     * Safely parses a price or currency double, checking for non-negativity.
     */
    public static double parseDouble(String valueStr, String fieldName) throws ValidationException {
        try {
            double val = Double.parseDouble(valueStr);
            if (val < 0) {
                throw new ValidationException(fieldName + " cannot be negative.");
            }
            return val;
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid decimal format for " + fieldName + ".");
        }
    }

    /**
     * Safely parses an integer ID or menu selection option.
     */
    public static int parseInt(String valueStr, String fieldName) throws ValidationException {
        try {
            return Integer.parseInt(valueStr);
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid integer format for " + fieldName + ".");
        }
    }
}
