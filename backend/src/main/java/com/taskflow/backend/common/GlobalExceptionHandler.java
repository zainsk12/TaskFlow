package com.taskflow.backend.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

/**
 * Translates exceptions into the consistent {@link ApiError} body documented in
 * {@code docs/API_SPEC.md} §1.3.
 *
 * <p>Covers the cases relevant to this phase — duplicate email, invalid
 * credentials, and bean-validation failures — plus {@link ResponseStatusException}
 * (used by the user module) and a catch-all {@code 500}. More domain exceptions
 * (e.g. {@code NotFoundException}, {@code ForbiddenException}) will plug in here
 * as later phases add them.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Duplicate email on registration → {@code 409 Conflict}. */
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiError> handleDuplicateEmail(DuplicateEmailException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    /** Bad email/password on login → {@code 401 Unauthorized}. */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request, null);
    }

    /** {@code @Valid} request-body failures → {@code 400 Bad Request} with field details. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ApiError.FieldErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toDetail)
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Validation failed", request, details);
    }

    /** Errors raised as {@link ResponseStatusException} (e.g. from the user module). */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException ex, HttpServletRequest request) {
        HttpStatusCode status = ex.getStatusCode();
        return build(status, ex.getReason() != null ? ex.getReason() : ex.getMessage(), request, null);
    }

    /** Anything unanticipated → {@code 500 Internal Server Error}. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request, null);
    }

    // ------------------------------------------------------------------

    private ApiError.FieldErrorDetail toDetail(FieldError fieldError) {
        String issue = fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "invalid value";
        return new ApiError.FieldErrorDetail(fieldError.getField(), issue);
    }

    private ResponseEntity<ApiError> build(HttpStatusCode status, String message, HttpServletRequest request,
                                           List<ApiError.FieldErrorDetail> details) {
        HttpStatus resolved = HttpStatus.valueOf(status.value());
        ApiError body = new ApiError(
                Instant.now(),
                resolved.value(),
                resolved.getReasonPhrase(),
                message,
                request.getRequestURI(),
                details
        );
        return ResponseEntity.status(status).body(body);
    }
}
