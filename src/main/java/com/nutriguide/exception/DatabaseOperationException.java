package com.nutriguide.exception;

import java.sql.SQLException;

/**
 * DatabaseOperationException: Checked exception wrapping low-level JDBC/SQL errors
 * encountered during database operations in the DAO layer.
 *
 * <p>Demonstrates Java Lab Manual concepts:</p>
 * <ul>
 *   <li><b>Exp 8</b> — {@code super} keyword: Constructor chaining via {@code super(message)} and
 *       {@code super(message, cause)} delegates initialization to the parent {@link Exception} class.</li>
 *   <li><b>Exp 14</b> — Exception handling: Wraps {@link SQLException} as a cause for clean
 *       propagation through DAO → Service → GUI layers.</li>
 *   <li><b>Exp 15</b> — {@code throws}: Methods in the DAO layer declare
 *       {@code throws DatabaseOperationException} in their signatures.</li>
 *   <li><b>Exp 16</b> — User-defined exception class with meaningful constructors.</li>
 * </ul>
 *
 * <p><b>Design Decision</b>: This is a <i>checked</i> exception (extends {@code Exception})
 * because database failures are recoverable — the caller can retry, show an error dialog,
 * or log and continue. Making it checked forces callers to handle it explicitly.</p>
 */
public class DatabaseOperationException extends Exception {

    /**
     * Constructs a DatabaseOperationException with a descriptive message.
     * Demonstrates: {@code super(message)} — constructor chaining to parent.
     *
     * @param message Human-readable description of what database operation failed
     */
    public DatabaseOperationException(String message) {
        super(message);
    }

    /**
     * Constructs a DatabaseOperationException wrapping an original cause.
     * Demonstrates: {@code super(message, cause)} — full constructor chaining.
     *
     * <p>This is the primary constructor used in DAO classes:</p>
     * <pre>
     * catch (SQLException e) {
     *     throw new DatabaseOperationException("Failed to insert user", e);
     * }
     * </pre>
     *
     * @param message Human-readable description of what database operation failed
     * @param cause   The original low-level exception (typically {@link SQLException})
     */
    public DatabaseOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Convenience method to extract the original SQL error code if the cause is a SQLException.
     *
     * @return MySQL vendor error code, or -1 if the cause is not a SQLException
     */
    public int getSqlErrorCode() {
        if (getCause() instanceof SQLException) {
            return ((SQLException) getCause()).getErrorCode();
        }
        return -1;
    }

    /**
     * Convenience method to extract the SQL state if the cause is a SQLException.
     *
     * @return SQL state string (e.g. "23000" for integrity constraint violation), or null
     */
    public String getSqlState() {
        if (getCause() instanceof SQLException) {
            return ((SQLException) getCause()).getSQLState();
        }
        return null;
    }
}
