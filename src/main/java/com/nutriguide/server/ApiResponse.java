package com.nutriguide.server;

/**
 * ApiResponse: Standard uniform JSON response wrapper for all API endpoints.
 */
public class ApiResponse {

    private boolean success;
    private String message;
    private Object data;
    private String error;

    public ApiResponse() {
    }

    public ApiResponse(boolean success, String message, Object data, String error) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.error = error;
    }

    public static ApiResponse ok(String message, Object data) {
        return new ApiResponse(true, message, data, null);
    }

    public static ApiResponse ok(Object data) {
        return new ApiResponse(true, null, data, null);
    }

    public static ApiResponse err(String error) {
        return new ApiResponse(false, null, null, error);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
