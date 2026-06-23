package com.taskflow.backend.common;

/**
 * Thrown when creating or renaming a resource would violate a uniqueness rule —
 * e.g. a category name that already exists for the same user. Mapped to
 * {@code 409 Conflict} by the global exception handler
 * (see {@code docs/API_SPEC.md} §5).
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
