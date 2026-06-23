package com.taskflow.backend.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request body for {@code PUT /api/v1/users/me} — see {@code docs/API_SPEC.md} §3.2.
 *
 * <p>All fields are <strong>optional</strong>: a {@code null} field means "leave
 * unchanged". {@code email} and {@code role} are deliberately absent — they are
 * immutable through this endpoint. When a field is present it must satisfy the
 * constraints below (a {@code null} value skips its own constraints, which is how
 * partial updates stay valid).
 *
 * @param name      new display name (2–60 chars) or {@code null} to keep current
 * @param avatarUrl new avatar URL (≤ 2048 chars) or {@code null} to keep current
 * @param timezone  new IANA timezone or {@code null} to keep current
 */
public record UpdateProfileRequest(

        @Size(min = 2, max = 60, message = "name must be between 2 and 60 characters")
        String name,

        @Size(max = 2048, message = "avatarUrl must be at most 2048 characters")
        String avatarUrl,

        // Light IANA check: either "UTC" or an "Area/Location" style identifier.
        @Pattern(
                regexp = "^(UTC|[A-Za-z]+(?:/[A-Za-z0-9_+-]+)+)$",
                message = "timezone must be a valid IANA timezone (e.g. Asia/Kolkata or UTC)"
        )
        String timezone
) {
}
