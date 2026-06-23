/**
 * Shared building blocks used across feature modules.
 *
 * <p>Currently holds the domain enums {@link com.taskflow.backend.common.Role},
 * {@link com.taskflow.backend.common.Priority}, and
 * {@link com.taskflow.backend.common.TaskStatus}.
 *
 * <p>Also holds the error-handling infrastructure: the {@code ApiError} payload
 * ({@code API_SPEC.md} §1.3), the {@code GlobalExceptionHandler}
 * ({@code @RestControllerAdvice}), and the domain exceptions wired into it so far
 * ({@code DuplicateEmailException} → 409, {@code InvalidCredentialsException} → 401).
 *
 * <p>TODO (future phases) — per {@code docs/ARCHITECTURE.md} §3:
 * <ul>
 *   <li>More domain exceptions (e.g. {@code NotFoundException}, {@code ForbiddenException}).</li>
 *   <li>A paged-response wrapper.</li>
 *   <li>Shared DTO &harr; domain mappers.</li>
 * </ul>
 */
package com.taskflow.backend.common;
