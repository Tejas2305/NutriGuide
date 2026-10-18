package com.nutriguide.util;

import com.nutriguide.exception.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * ValidationUtil: Static utility class providing input validation for NutriGuide.
 *
 * Used across the GUI and Service layers before passing data to the DAO layer.
 * Demonstrates basic Java concepts: static methods, final class, private constructor,
 * and custom exception throwing.
 */
public final class ValidationUtil {

    // Regex pattern for validating email addresses: local-part@domain.tld
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Domain range constants
    public static final int MIN_AGE = 1;
    public static final int MAX_AGE = 150;

    public static final double MIN_HEIGHT_CM = 30.0;
    public static final double MAX_HEIGHT_CM = 300.0;

    public static final double MIN_WEIGHT_KG = 1.0;
    public static final double MAX_WEIGHT_KG = 500.0;

    // Private constructor prevents instantiation
    private ValidationUtil() {
    }

    // ========================================================================
    // String & Email Validation
    // ========================================================================

    /**
     * Checks if a string is non-null and not empty after trimming.
     */
    public static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Validates that a string is not blank; throws ValidationException otherwise.
     */
    public static boolean requireNonBlank(String value, String fieldName) {
        if (!isNotBlank(value)) {
            throw new ValidationException(fieldName, fieldName + " is required and cannot be blank.");
        }
        return true;
    }

    /**
     * Checks if an email string matches the valid email format.
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validates email format; throws ValidationException on mismatch.
     */
    public static boolean requireValidEmail(String email) {
        if (!isValidEmail(email)) {
            throw new ValidationException("email", "Invalid email format: " + email);
        }
        return true;
    }

    // ========================================================================
    // Numeric Validations
    // ========================================================================

    /**
     * Checks if an integer is strictly positive (> 0).
     */
    public static boolean isPositiveInt(int value) {
        return value > 0;
    }

    /**
     * Checks if a double is strictly positive (> 0.0).
     */
    public static boolean isPositiveDouble(double value) {
        return value > 0.0;
    }

    /**
     * Checks if a BigDecimal is strictly positive (> 0).
     */
    public static boolean isPositiveBigDecimal(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Checks if a double is non-negative (>= 0.0).
     */
    public static boolean isNonNegativeDouble(double value) {
        return value >= 0.0;
    }

    /**
     * Checks if a BigDecimal is non-negative (>= 0).
     */
    public static boolean isNonNegativeBigDecimal(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) >= 0;
    }

    // ========================================================================
    // Domain Validations: Age, Height, Weight, Quantity
    // ========================================================================

    /**
     * Checks if age falls within [MIN_AGE, MAX_AGE].
     */
    public static boolean isValidAge(int age) {
        return age >= MIN_AGE && age <= MAX_AGE;
    }

    /**
     * Validates age; throws ValidationException if out of bounds.
     */
    public static boolean requireValidAge(int age) {
        if (!isValidAge(age)) {
            throw new ValidationException("age", "Age must be between " + MIN_AGE + " and " + MAX_AGE + ". Got: " + age);
        }
        return true;
    }

    /**
     * Checks if height falls within [MIN_HEIGHT_CM, MAX_HEIGHT_CM].
     */
    public static boolean isValidHeight(double heightCm) {
        return heightCm >= MIN_HEIGHT_CM && heightCm <= MAX_HEIGHT_CM;
    }

    /**
     * Validates height; throws ValidationException if out of bounds.
     */
    public static boolean requireValidHeight(double heightCm) {
        if (!isValidHeight(heightCm)) {
            throw new ValidationException("height", "Height must be between " + MIN_HEIGHT_CM + " and " + MAX_HEIGHT_CM + " cm. Got: " + heightCm);
        }
        return true;
    }

    /**
     * Checks if weight falls within [MIN_WEIGHT_KG, MAX_WEIGHT_KG].
     */
    public static boolean isValidWeight(double weightKg) {
        return weightKg >= MIN_WEIGHT_KG && weightKg <= MAX_WEIGHT_KG;
    }

    /**
     * Validates weight; throws ValidationException if out of bounds.
     */
    public static boolean requireValidWeight(double weightKg) {
        if (!isValidWeight(weightKg)) {
            throw new ValidationException("weight", "Weight must be between " + MIN_WEIGHT_KG + " and " + MAX_WEIGHT_KG + " kg. Got: " + weightKg);
        }
        return true;
    }

    /**
     * Checks if meal or plan item quantity is strictly positive (> 0.0).
     */
    public static boolean isValidQuantity(double quantity) {
        return quantity > 0.0;
    }

    /**
     * Validates quantity; throws ValidationException if <= 0.0.
     */
    public static boolean requireValidQuantity(double quantity) {
        if (!isValidQuantity(quantity)) {
            throw new ValidationException("quantity", "Quantity must be greater than 0. Got: " + quantity);
        }
        return true;
    }

    // ========================================================================
    // Date Validations
    // ========================================================================

    /**
     * Validates that a date is non-null; throws ValidationException otherwise.
     */
    public static boolean requireNonNullDate(LocalDate date, String fieldName) {
        if (date == null) {
            throw new ValidationException(fieldName, fieldName + " date is required and cannot be null.");
        }
        return true;
    }

    /**
     * Validates that endDate is not before startDate.
     */
    public static boolean requireValidDateRange(LocalDate startDate, LocalDate endDate) {
        requireNonNullDate(startDate, "startDate");
        requireNonNullDate(endDate, "endDate");
        if (endDate.isBefore(startDate)) {
            throw new ValidationException("dateRange", "End date (" + endDate + ") must not be before start date (" + startDate + ").");
        }
        return true;
    }
}
