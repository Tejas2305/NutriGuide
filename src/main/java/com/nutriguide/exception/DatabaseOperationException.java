package com.nutriguide.exception;

import java.sql.SQLException;

/**
 * DatabaseOperationException - thrown when a database operation fails.
 *
 * This is a "checked exception" (extends Exception), which means any method
 * that calls DAO methods MUST either catch this or declare it with "throws".
 *
 * We wrap the original SQLException inside this so the rest of the app
 * doesn't need to know about low-level SQL errors.
 */
public class DatabaseOperationException extends Exception {

    /**
     * Creates a DatabaseOperationException with just a message.
     */
    public DatabaseOperationException(String message) {
        super(message);
    }

    /**
     * Creates a DatabaseOperationException with a message and the original cause.
     * This is used in DAO classes like:
     *   catch (SQLException e) {
     *       throw new DatabaseOperationException("Failed to insert user", e);
     *   }
     */
    public DatabaseOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
