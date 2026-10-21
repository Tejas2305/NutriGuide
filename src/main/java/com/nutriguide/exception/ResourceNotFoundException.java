package com.nutriguide.exception;

/**
 * ResourceNotFoundException - thrown when we look for something in the database
 * but it doesn't exist (e.g., looking for a user by ID that doesn't exist).
 *
 * This is an "unchecked exception" (extends RuntimeException).
 */
public class ResourceNotFoundException extends RuntimeException {

    // The type of resource that was not found (e.g. "User", "Food", "Meal")
    private String resourceType;

    // The ID or value that was searched for
    private Object resourceId;

    /**
     * Creates a ResourceNotFoundException with the resource type and ID.
     * Example: new ResourceNotFoundException("User", 5)
     * This will produce message: "User not found with identifier: 5"
     */
    public ResourceNotFoundException(String resourceType, Object resourceId) {
        super(resourceType + " not found with identifier: " + resourceId);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    /**
     * Creates a ResourceNotFoundException with a custom message.
     */
    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceType = null;
        this.resourceId = null;
    }

    /**
     * Returns what type of resource was not found.
     */
    public String getResourceType() {
        return resourceType;
    }

    /**
     * Returns the ID that was searched for.
     */
    public Object getResourceId() {
        return resourceId;
    }
}
