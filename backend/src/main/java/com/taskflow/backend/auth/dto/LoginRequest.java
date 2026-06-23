package com.taskflow.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for {@code POST /api/v1/auth/login} — see {@code docs/API_SPEC.md} §2.2.
 *
 * @param email    login identifier (required, valid email)
 * @param password plain-text password (required)
 */
public record LoginRequest(

        @NotBlank(message = "email must not be blank")
        @Email(message = "email must be a valid email address")
        String email,

        @NotBlank(message = "password must not be blank")
        String password
) {
}
