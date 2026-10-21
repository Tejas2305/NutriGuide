package com.nutriguide.exception;

/**
 * AuthenticationException - thrown when login fails.
 * For example: wrong password, email not found, etc.
 *
 * This is an "unchecked exception" (extends RuntimeException),
 * which means methods don't have to declare it with "throws".
 */
public class AuthenticationException extends RuntimeException {

    /**
     * Creates an AuthenticationException with a message.
     * Example: new AuthenticationException("Invalid email or password.")
     */
    public AuthenticationException(String message) {
        super(message);
    }

    /**
     * Creates an AuthenticationException with a message and the original cause.
     */
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
