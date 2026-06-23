package com.taskflow.backend.common;

/**
 * Thrown when a supplied JWT (e.g. a refresh token at {@code /auth/refresh}) is
 * missing, malformed, expired, or of the wrong kind. Mapped to
 * {@code 401 Unauthorized} by the global exception handler
 * (see {@code docs/API_SPEC.md} §2.3).
 */
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message) {
        super(message);
    }
}
