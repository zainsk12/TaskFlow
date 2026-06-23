package com.taskflow.backend.common;

/**
 * Thrown when registration is attempted with an email that already has an
 * account. Mapped to {@code 409 Conflict} by the global exception handler
 * (see {@code docs/API_SPEC.md} §2.1).
 */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String message) {
        super(message);
    }
}
