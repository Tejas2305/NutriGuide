package com.nutriguide.util;

import com.nutriguide.exception.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * ValidationUtil - a utility class with static methods for input validation.
 *
 * All methods are static, so you call them like:
 *   ValidationUtil.requireValidEmail("test@example.com");
 *
 * Methods that "require" something will throw a ValidationException if the check fails.
 * Methods that "is..." just return true or false.
 */
public final class ValidationUtil {

    // Email pattern: checks for format like "user@domain.com"
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Valid ranges for age, height, and weight
    public static final int MIN_AGE = 1;
    public static final int MAX_AGE = 150;
    public static final double MIN_HEIGHT_CM = 30.0;
    public static final double MAX_HEIGHT_CM = 300.0;
    public static final double MIN_WEIGHT_KG = 1.0;
    public static final double MAX_WEIGHT_KG = 500.0;

    // Private constructor: prevents creating objects of this class
    private ValidationUtil() {
    }

    // ========== String Validation ==========

    /**
     * Returns true if the string is not null and not empty (after trimming spaces).
     */
    public static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Throws ValidationException if the string is blank.
     */
    public static boolean requireNonBlank(String value, String fieldName) {
        if (!isNotBlank(value)) {
            throw new ValidationException(fieldName, fieldName + " is required and cannot be blank.");
        }
        return true;
    }

    /**
     * Returns true if the email matches a valid format.
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Throws ValidationException if the email is invalid.
     */
    public static boolean requireValidEmail(String email) {
        if (!isValidEmail(email)) {
            throw new ValidationException("email", "Invalid email format: " + email);
        }
        return true;
    }

    // ========== Number Validation ==========

    /**
     * Returns true if the integer is greater than 0.
     */
    public static boolean isPositiveInt(int value) {
        return value > 0;
    }

    /**
     * Returns true if the double is greater than 0.
     */
    public static boolean isPositiveDouble(double value) {
        return value > 0.0;
    }

    /**
     * Returns true if the BigDecimal is not null and greater than 0.
     */
    public static boolean isPositiveBigDecimal(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Returns true if the double is 0 or more (not negative).
     */
    public static boolean isNonNegativeDouble(double value) {
        return value >= 0.0;
    }

    /**
     * Returns true if the BigDecimal is not null and 0 or more.
     */
    public static boolean isNonNegativeBigDecimal(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) >= 0;
    }

    // ========== Domain Validation: Age, Height, Weight ==========

    /**
     * Returns true if the age is between MIN_AGE and MAX_AGE.
     */
    public static boolean isValidAge(int age) {
        return age >= MIN_AGE && age <= MAX_AGE;
    }

    /**
     * Throws ValidationException if the age is out of range.
     */
    public static boolean requireValidAge(int age) {
        if (!isValidAge(age)) {
            throw new ValidationException("age", "Age must be between " + MIN_AGE + " and " + MAX_AGE + ". Got: " + age);
        }
        return true;
    }

    /**
     * Returns true if the height is within valid range.
     */
    public static boolean isValidHeight(double heightCm) {
        return heightCm >= MIN_HEIGHT_CM && heightCm <= MAX_HEIGHT_CM;
    }

    /**
     * Throws ValidationException if the height is out of range.
     */
    public static boolean requireValidHeight(double heightCm) {
        if (!isValidHeight(heightCm)) {
            throw new ValidationException("height", "Height must be between " + MIN_HEIGHT_CM + " and " + MAX_HEIGHT_CM + " cm. Got: " + heightCm);
        }
        return true;
    }

    /**
     * Returns true if the weight is within valid range.
     */
    public static boolean isValidWeight(double weightKg) {
        return weightKg >= MIN_WEIGHT_KG && weightKg <= MAX_WEIGHT_KG;
    }

    /**
     * Throws ValidationException if the weight is out of range.
     */
    public static boolean requireValidWeight(double weightKg) {
        if (!isValidWeight(weightKg)) {
            throw new ValidationException("weight", "Weight must be between " + MIN_WEIGHT_KG + " and " + MAX_WEIGHT_KG + " kg. Got: " + weightKg);
        }
        return true;
    }

    /**
     * Returns true if the quantity is greater than 0.
     */
    public static boolean isValidQuantity(double quantity) {
        return quantity > 0.0;
    }

    /**
     * Throws ValidationException if the quantity is 0 or less.
     */
    public static boolean requireValidQuantity(double quantity) {
        if (!isValidQuantity(quantity)) {
            throw new ValidationException("quantity", "Quantity must be greater than 0. Got: " + quantity);
        }
        return true;
    }

    // ========== Date Validation ==========

    /**
     * Throws ValidationException if the date is null.
     */
    public static boolean requireNonNullDate(LocalDate date, String fieldName) {
        if (date == null) {
            throw new ValidationException(fieldName, fieldName + " date is required and cannot be null.");
        }
        return true;
    }

    /**
     * Throws ValidationException if endDate is before startDate.
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
