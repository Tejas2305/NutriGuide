package com.nutriguide.exception;

/**
 * ValidationException - thrown when user input fails validation.
 * For example: empty name, invalid email, negative quantity, etc.
 *
 * This is an "unchecked exception" (extends RuntimeException).
 * The fieldName tells us which input field caused the problem.
 */
public class ValidationException extends RuntimeException {

    // The name of the field that failed (e.g. "email", "age", "quantity")
    private String fieldName;

    /**
     * Creates a ValidationException for a specific field.
     * Example: new ValidationException("email", "Invalid email format.")
     */
    public ValidationException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }

    /**
     * Creates a ValidationException without a specific field name.
     */
    public ValidationException(String message) {
        super(message);
        this.fieldName = null;
    }

    /**
     * Returns which field caused the validation failure.
     */
    public String getFieldName() {
        return fieldName;
    }
}
