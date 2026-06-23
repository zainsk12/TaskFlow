package com.taskflow.backend.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Consistent error body returned for every failed request — the shape documented
 * in {@code docs/API_SPEC.md} §1.3.
 *
 * <p>{@code details} is only present for validation failures (one entry per
 * rejected field); it is omitted from the JSON otherwise.
 *
 * @param timestamp when the error was produced (UTC)
 * @param status    HTTP status code
 * @param error     HTTP reason phrase (e.g. {@code "Bad Request"})
 * @param message   human-readable summary
 * @param path      request path that produced the error
 * @param details   per-field validation problems, or {@code null}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldErrorDetail> details
) {

    /** A single field-level validation problem. */
    public record FieldErrorDetail(String field, String issue) {
    }
}
