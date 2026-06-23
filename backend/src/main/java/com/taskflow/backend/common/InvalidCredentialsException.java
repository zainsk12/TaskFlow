package com.taskflow.backend.common;

/**
 * Thrown when login fails because the email is unknown or the password does not
 * match. Mapped to {@code 401 Unauthorized} by the global exception handler
 * (see {@code docs/API_SPEC.md} §2.2).
 *
 * <p>The message is deliberately generic ("Invalid email or password") so the
 * response never reveals whether the email exists.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
