/**
 * Request and response DTOs for the user profile module.
 *
 * <p>DTOs decouple the wire contract from the {@code User} persistence model —
 * notably they never expose {@code passwordHash}. Defined here (see
 * {@code docs/API_SPEC.md} §3):
 * <ul>
 *   <li>{@code UserResponse} — safe profile view (id, name, email, role, avatarUrl, timezone, timestamps).</li>
 *   <li>{@code UpdateProfileRequest} — name, avatarUrl, timezone (all optional, validated).</li>
 *   <li>{@code ChangePasswordRequest} — currentPassword, newPassword.</li>
 * </ul>
 */
package com.taskflow.backend.user.dto;
