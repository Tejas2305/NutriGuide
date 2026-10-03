package com.nutriguide.exception;

/**
 * AuthenticationException: Thrown when user authentication fails
 * (invalid credentials, account not found, etc.).
 *
 * <p><b>Design Decision</b>: This is an <i>unchecked</i> exception (extends {@code RuntimeException})
 * because authentication failures are typically terminal for the current operation —
 * the caller cannot "fix" bad credentials programmatically, so forcing a checked exception
 * would add boilerplate without value. The Swing GUI catches this at the login boundary.</p>
 *
 * <p>Demonstrates Java Lab Exp 16 — User-defined exception with constructor chaining.</p>
 */
public class AuthenticationException extends RuntimeException {

    /**
     * Constructs an AuthenticationException with a descriptive message.
     *
     * @param message Description of the authentication failure (e.g. "Invalid email or password")
     */
    public AuthenticationException(String message) {
        super(message);
    }

    /**
     * Constructs an AuthenticationException with a message and an underlying cause.
     *
     * @param message Description of the authentication failure
     * @param cause   The original exception that triggered this authentication failure
     */
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
