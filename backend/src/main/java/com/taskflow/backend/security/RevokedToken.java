package com.taskflow.backend.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Denylist entry for a revoked refresh token — maps to the {@code revoked_tokens}
 * collection.
 *
 * <p>Created by {@code AuthService#logout} when a valid refresh token is
 * presented at logout, and checked by {@code AuthService#refresh} before a new
 * access token is issued — see {@code docs/DATABASE.md} §9.
 *
 * <p>{@code expiresAt} mirrors the revoked token's own {@code exp} claim, so
 * the record only needs to exist for as long as the token itself would
 * otherwise still be accepted; a {@code expireAfterSeconds = 0} TTL index on
 * this field lets MongoDB delete the entry automatically once that instant
 * passes, instead of the denylist growing forever.
 *
 * <p>Auto index creation is off in this project (see
 * {@code application.properties}), so — like the existing {@code users.email_1}
 * index — the unique index on {@code jti} and the TTL index on
 * {@code expiresAt} must be created once, out of band (see
 * {@code docs/DATABASE.md} §9). The service layer does not depend on the
 * unique index for correctness (it checks {@code existsByJti} before saving,
 * the same defence-in-depth pattern used for category name uniqueness).
 */
@Document(collection = "revoked_tokens")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevokedToken {

    @Id
    private String id;

    /** The revoked token's {@code jti} claim. */
    @Indexed(unique = true)
    private String jti;

    /** Owner of the revoked token, for traceability. References {@code users._id}. */
    private String userId;

    /** Mirrors the token's own {@code exp} claim. TTL-indexed so the record self-cleans. */
    @Indexed(expireAfterSeconds = 0)
    private Instant expiresAt;
}
