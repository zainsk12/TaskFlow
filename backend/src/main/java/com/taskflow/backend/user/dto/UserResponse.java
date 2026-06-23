package com.taskflow.backend.user.dto;

import com.taskflow.backend.common.Role;

import java.time.Instant;

/**
 * Safe, read-only view of a {@link com.taskflow.backend.user.User} returned to
 * clients — see {@code docs/API_SPEC.md} §3.1.
 *
 * <p>Intentionally omits {@code passwordHash}: this DTO is the only thing the
 * API ever serialises for a user, so the hash can never leak over the wire.
 *
 * @param id        the user's id ({@code _id})
 * @param name      display name
 * @param email     login identifier
 * @param role      {@link Role#USER} or {@link Role#ADMIN}
 * @param avatarUrl optional profile image URL (may be {@code null})
 * @param timezone  IANA timezone
 * @param createdAt account creation timestamp
 * @param updatedAt last profile-update timestamp
 */
public record UserResponse(
        String id,
        String name,
        String email,
        Role role,
        String avatarUrl,
        String timezone,
        Instant createdAt,
        Instant updatedAt
) {
}
