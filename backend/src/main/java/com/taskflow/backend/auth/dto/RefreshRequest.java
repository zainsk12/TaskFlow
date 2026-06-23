package com.taskflow.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for {@code POST /api/v1/auth/refresh} and {@code /auth/logout} —
 * see {@code docs/API_SPEC.md} §2.3 / §2.4.
 *
 * @param refreshToken the refresh JWT previously issued at login/register (required)
 */
public record RefreshRequest(

        @NotBlank(message = "refreshToken must not be blank")
        String refreshToken
) {
}
