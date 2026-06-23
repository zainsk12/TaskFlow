package com.taskflow.backend.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for {@code PATCH /api/v1/users/me/password} — see
 * {@code docs/API_SPEC.md} §3.3.
 *
 * <p>Both fields are required. {@code newPassword} carries the strength rule
 * (length); the actual comparison of {@code currentPassword} against the stored
 * hash, and the hashing of {@code newPassword}, happen in the service layer once
 * a {@code PasswordEncoder} is available (security phase).
 *
 * @param currentPassword the user's existing password (verified, never stored)
 * @param newPassword     the replacement password (8–72 chars)
 */
public record ChangePasswordRequest(

        @NotBlank(message = "currentPassword must not be blank")
        String currentPassword,

        // 72 is BCrypt's effective maximum input length.
        @NotBlank(message = "newPassword must not be blank")
        @Size(min = 8, max = 72, message = "newPassword must be between 8 and 72 characters")
        String newPassword
) {
}
