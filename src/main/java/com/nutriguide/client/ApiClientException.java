package com.nutriguide.client;

/**
 * ApiClientException: Checked exception for client-side API communication errors,
 * HTTP status errors (400, 401, 403, 404, 500), and backend connectivity failures.
 */
public class ApiClientException extends Exception {

    private final int statusCode;

    public ApiClientException(String message) {
        super(message);
        this.statusCode = 0;
    }

    public ApiClientException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public ApiClientException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
