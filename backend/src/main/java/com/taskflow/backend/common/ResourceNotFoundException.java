package com.taskflow.backend.common;

/**
 * Thrown when a requested resource does not exist <em>or</em> is not owned by the
 * authenticated user. Mapped to {@code 404 Not Found} by the global exception
 * handler.
 *
 * <p>The "not found" and "not owned" cases are deliberately indistinguishable so
 * the API never reveals the existence of another user's resource.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
