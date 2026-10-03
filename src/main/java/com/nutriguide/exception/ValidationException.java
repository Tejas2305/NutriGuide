package com.nutriguide.exception;

/**
 * ValidationException: Thrown when user input or business-rule validation fails.
 *
 * <p>Used by the service layer and GUI to reject invalid data before it reaches the database
 * (e.g. negative quantity, empty name, out-of-range age).</p>
 *
 * <p><b>Design Decision</b>: Unchecked (extends {@code RuntimeException}) because
 * validation failures are programming/input errors that should be caught at the boundary
 * (GUI form submission) rather than polluting every method signature with throws clauses.</p>
 *
 * <p>Demonstrates Java Lab Exp 16 — User-defined exception with a field name for
 * targeted error reporting in Swing dialogs.</p>
 */
public class ValidationException extends RuntimeException {

    /** The name of the field or parameter that failed validation */
    private final String fieldName;

    /**
     * Constructs a ValidationException for a specific field.
     *
     * @param fieldName The field or parameter that failed validation (e.g. "email", "quantity")
     * @param message   Human-readable explanation of why validation failed
     */
    public ValidationException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }

    /**
     * Constructs a ValidationException without a specific field name.
     *
     * @param message Human-readable explanation of the validation failure
     */
    public ValidationException(String message) {
        super(message);
        this.fieldName = null;
    }

    /**
     * Returns the field name that triggered this validation error.
     *
     * @return Field name, or null if no specific field was identified
     */
    public String getFieldName() {
        return fieldName;
    }
}
