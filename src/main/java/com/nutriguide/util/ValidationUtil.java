package com.nutriguide.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * ValidationUtil: Reusable static input-validation utility for the NutriGuide application.
 *
 * <p>Provides general-purpose validation methods for strings, emails, numeric ranges,
 * and domain-specific values (age, height, weight, quantity). These are used by
 * the service layer and GUI before data reaches the DAO/database layer.</p>
 *
 * <p>No database or business-specific logic resides here — only structural input validation.</p>
 *
 * <p>Demonstrates Java Lab Manual concepts:</p>
 * <ul>
 *   <li><b>Exp 3</b> — Static methods and static members ({@code static final} pattern,
 *       static utility methods with no instance state).</li>
 *   <li><b>Exp 9</b> — {@code final} keyword on the class (prevents subclassing)
 *       and on constants ({@code static final} prevents mutation).</li>
 *   <li><b>Exp 12</b> — User-defined packages ({@code com.nutriguide.util}).</li>
 * </ul>
 */
public final class ValidationUtil {

    /**
     * Compiled regex pattern for basic email format validation.
     * Matches: local-part@domain.tld (at least 2-char TLD).
     */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /** Minimum allowed age for user registration */
    public static final int MIN_AGE = 1;

    /** Maximum allowed age for user registration */
    public static final int MAX_AGE = 150;

    /** Minimum allowed height in centimeters */
    public static final double MIN_HEIGHT_CM = 30.0;

    /** Maximum allowed height in centimeters */
    public static final double MAX_HEIGHT_CM = 300.0;

    /** Minimum allowed weight in kilograms */
    public static final double MIN_WEIGHT_KG = 1.0;

    /** Maximum allowed weight in kilograms */
    public static final double MAX_WEIGHT_KG = 500.0;

    /**
     * Private constructor prevents instantiation.
     * All methods are static utility methods.
     */
    private ValidationUtil() {
        // Utility class: not meant to be instantiated
    }

    // ========================================================================
    // String Validations
    // ========================================================================

    /**
     * Checks whether a string is non-null and contains non-whitespace content.
     *
     * @param value The string to validate
     * @return {@code true} if the string is non-null, non-empty, and not purely whitespace
     */
    public static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Validates that a required string field is present (non-null, non-blank).
     *
     * @param value     The string to validate
     * @param fieldName The field name for error messaging
     * @return {@code true} if valid
     * @throws com.nutriguide.exception.ValidationException if null or blank
     */
    public static boolean requireNonBlank(String value, String fieldName) {
        if (!isNotBlank(value)) {
            throw new com.nutriguide.exception.ValidationException(fieldName,
                    fieldName + " is required and cannot be blank.");
        }
        return true;
    }

    // ========================================================================
    // Email Validation
    // ========================================================================

    /**
     * Validates email format against a standard pattern.
     *
     * @param email The email address to validate
     * @return {@code true} if the email matches a valid format
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validates email format, throwing on failure.
     *
     * @param email The email address to validate
     * @return {@code true} if valid
     * @throws com.nutriguide.exception.ValidationException if the email format is invalid
     */
    public static boolean requireValidEmail(String email) {
        if (!isValidEmail(email)) {
            throw new com.nutriguide.exception.ValidationException("email",
                    "Invalid email format: " + email);
        }
        return true;
    }

    // ========================================================================
    // Numeric Validations
    // ========================================================================

    /**
     * Checks whether an integer value is strictly positive (> 0).
     *
     * @param value The integer to validate
     * @return {@code true} if value > 0
     */
    public static boolean isPositiveInt(int value) {
        return value > 0;
    }

    /**
     * Checks whether a double value is strictly positive (> 0).
     *
     * @param value The double to validate
     * @return {@code true} if value > 0
     */
    public static boolean isPositiveDouble(double value) {
        return value > 0.0;
    }

    /**
     * Checks whether a BigDecimal value is strictly positive (> 0).
     *
     * @param value The BigDecimal to validate
     * @return {@code true} if non-null and value > 0
     */
    public static boolean isPositiveBigDecimal(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Checks whether a double value is non-negative (>= 0).
     * Used for nutritional values where zero is valid (e.g., 0g carbs in chicken).
     *
     * @param value The double to validate
     * @return {@code true} if value >= 0
     */
    public static boolean isNonNegativeDouble(double value) {
        return value >= 0.0;
    }

    /**
     * Checks whether a BigDecimal value is non-negative (>= 0).
     *
     * @param value The BigDecimal to validate
     * @return {@code true} if non-null and value >= 0
     */
    public static boolean isNonNegativeBigDecimal(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) >= 0;
    }

    // ========================================================================
    // Domain-Specific Validations (Age, Height, Weight, Quantity)
    // ========================================================================

    /**
     * Validates that age is within a reasonable range.
     *
     * @param age The age value to validate
     * @return {@code true} if age is within [{@link #MIN_AGE}, {@link #MAX_AGE}]
     */
    public static boolean isValidAge(int age) {
        return age >= MIN_AGE && age <= MAX_AGE;
    }

    /**
     * Validates age, throwing on failure.
     *
     * @param age The age to validate
     * @return {@code true} if valid
     * @throws com.nutriguide.exception.ValidationException if age is out of range
     */
    public static boolean requireValidAge(int age) {
        if (!isValidAge(age)) {
            throw new com.nutriguide.exception.ValidationException("age",
                    "Age must be between " + MIN_AGE + " and " + MAX_AGE + ". Got: " + age);
        }
        return true;
    }

    /**
     * Validates that height (cm) is within a reasonable range.
     *
     * @param heightCm Height in centimeters
     * @return {@code true} if within [{@link #MIN_HEIGHT_CM}, {@link #MAX_HEIGHT_CM}]
     */
    public static boolean isValidHeight(double heightCm) {
        return heightCm >= MIN_HEIGHT_CM && heightCm <= MAX_HEIGHT_CM;
    }

    /**
     * Validates height, throwing on failure.
     *
     * @param heightCm Height in centimeters
     * @return {@code true} if valid
     * @throws com.nutriguide.exception.ValidationException if out of range
     */
    public static boolean requireValidHeight(double heightCm) {
        if (!isValidHeight(heightCm)) {
            throw new com.nutriguide.exception.ValidationException("height",
                    "Height must be between " + MIN_HEIGHT_CM + " and " + MAX_HEIGHT_CM + " cm. Got: " + heightCm);
        }
        return true;
    }

    /**
     * Validates that weight (kg) is within a reasonable range.
     *
     * @param weightKg Weight in kilograms
     * @return {@code true} if within [{@link #MIN_WEIGHT_KG}, {@link #MAX_WEIGHT_KG}]
     */
    public static boolean isValidWeight(double weightKg) {
        return weightKg >= MIN_WEIGHT_KG && weightKg <= MAX_WEIGHT_KG;
    }

    /**
     * Validates weight, throwing on failure.
     *
     * @param weightKg Weight in kilograms
     * @return {@code true} if valid
     * @throws com.nutriguide.exception.ValidationException if out of range
     */
    public static boolean requireValidWeight(double weightKg) {
        if (!isValidWeight(weightKg)) {
            throw new com.nutriguide.exception.ValidationException("weight",
                    "Weight must be between " + MIN_WEIGHT_KG + " and " + MAX_WEIGHT_KG + " kg. Got: " + weightKg);
        }
        return true;
    }

    /**
     * Validates that a meal item or plan item quantity is strictly positive.
     *
     * @param quantity The quantity to validate
     * @return {@code true} if quantity > 0
     */
    public static boolean isValidQuantity(double quantity) {
        return quantity > 0.0;
    }

    /**
     * Validates quantity, throwing on failure.
     *
     * @param quantity The quantity to validate
     * @return {@code true} if valid
     * @throws com.nutriguide.exception.ValidationException if quantity <= 0
     */
    public static boolean requireValidQuantity(double quantity) {
        if (!isValidQuantity(quantity)) {
            throw new com.nutriguide.exception.ValidationException("quantity",
                    "Quantity must be greater than 0. Got: " + quantity);
        }
        return true;
    }

    // ========================================================================
    // Date Validations
    // ========================================================================

    /**
     * Validates that a date is not null.
     *
     * @param date      The date to validate
     * @param fieldName The field name for error messaging
     * @return {@code true} if the date is non-null
     */
    public static boolean requireNonNullDate(LocalDate date, String fieldName) {
        if (date == null) {
            throw new com.nutriguide.exception.ValidationException(fieldName,
                    fieldName + " date is required and cannot be null.");
        }
        return true;
    }

    /**
     * Validates that an end date is not before a start date.
     *
     * @param startDate The start date
     * @param endDate   The end date
     * @return {@code true} if endDate >= startDate
     * @throws com.nutriguide.exception.ValidationException if endDate is before startDate
     */
    public static boolean requireValidDateRange(LocalDate startDate, LocalDate endDate) {
        requireNonNullDate(startDate, "startDate");
        requireNonNullDate(endDate, "endDate");
        if (endDate.isBefore(startDate)) {
            throw new com.nutriguide.exception.ValidationException("dateRange",
                    "End date (" + endDate + ") must not be before start date (" + startDate + ").");
        }
        return true;
    }
}
