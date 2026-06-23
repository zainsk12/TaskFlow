package com.taskflow.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for {@code POST /api/v1/auth/register} — see {@code docs/API_SPEC.md} §2.1.
 *
 * @param name     display name (required, 2–60 chars)
 * @param email    login identifier (required, valid email; stored lowercased)
 * @param password plain-text password (required, ≥ 8 chars) — never stored as-is,
 *                 only its BCrypt hash is persisted
 */
public record RegisterRequest(

        @NotBlank(message = "name must not be blank")
        @Size(min = 2, max = 60, message = "name must be between 2 and 60 characters")
        String name,

        @NotBlank(message = "email must not be blank")
        @Email(message = "email must be a valid email address")
        String email,

        @NotBlank(message = "password must not be blank")
        @Size(min = 8, max = 72, message = "password must be between 8 and 72 characters")
        String password
) {
}
