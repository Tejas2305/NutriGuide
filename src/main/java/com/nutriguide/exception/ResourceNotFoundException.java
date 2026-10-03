package com.nutriguide.exception;

/**
 * ResourceNotFoundException: Thrown when a requested entity (user, food, meal, etc.)
 * does not exist in the database.
 *
 * <p>Used by DAO and service layers when a lookup by ID or unique key returns no results.</p>
 *
 * <p><b>Design Decision</b>: Unchecked (extends {@code RuntimeException}) because a missing
 * resource typically indicates a logic error or stale reference rather than a recoverable
 * condition. The GUI layer catches this to display "not found" messages.</p>
 *
 * <p>Demonstrates Java Lab Exp 16 — User-defined exception with resource-type tracking.</p>
 */
public class ResourceNotFoundException extends RuntimeException {

    /** The type of resource that was not found (e.g. "User", "Food", "Meal") */
    private final String resourceType;

    /** The identifier that was used in the failed lookup */
    private final Object resourceId;

    /**
     * Constructs a ResourceNotFoundException for a specific resource type and identifier.
     *
     * @param resourceType The entity type (e.g. "User", "Food", "Meal")
     * @param resourceId   The identifier that was searched for (e.g. userId, email)
     */
    public ResourceNotFoundException(String resourceType, Object resourceId) {
        super(resourceType + " not found with identifier: " + resourceId);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    /**
     * Constructs a ResourceNotFoundException with a custom message.
     *
     * @param message Custom error message
     */
    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceType = null;
        this.resourceId = null;
    }

    /**
     * Returns the type of resource that was not found.
     *
     * @return Resource type string, or null
     */
    public String getResourceType() {
        return resourceType;
    }

    /**
     * Returns the identifier used in the failed lookup.
     *
     * @return Resource identifier, or null
     */
    public Object getResourceId() {
        return resourceId;
    }
}
