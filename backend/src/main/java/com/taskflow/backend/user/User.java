package com.taskflow.backend.user;

import com.taskflow.backend.common.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Account record and profile data — maps to the {@code users} collection.
 *
 * <p>Field definitions follow {@code docs/DATABASE.md} §2. The {@code passwordHash}
 * is set only by the backend (BCrypt) and must never be exposed through a DTO —
 * mapping to the wire happens via {@link UserMapper}, which omits it.
 *
 * <p>Bean Validation constraints are applied at the DTO boundary (see the
 * {@code user.dto} package), not on the document, since Spring Data MongoDB does
 * not validate documents on save by default. Data-integrity guarantees on this
 * collection come from the unique {@code email} index and the service layer.
 *
 * <p>TODO (future phases):
 * <ul>
 *   <li>{@code passwordHash} is currently populated only by the auth/registration
 *       flow, which is not implemented yet (see {@code AuthService}).</li>
 * </ul>
 */
@Document(collection = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private String id;

    /** Display name. 2–60 chars (enforced at the DTO boundary). */
    private String name;

    /** Login identifier. Stored lowercased and unique. */
    @Indexed(unique = true)
    private String email;

    /** BCrypt hash. Set only by the backend; never returned by the API. */
    private String passwordHash;

    /** {@link Role#USER} or {@link Role#ADMIN}. Defaults to {@code USER}. */
    @Builder.Default
    @Indexed
    private Role role = Role.USER;

    /** Optional profile image URL. */
    private String avatarUrl;

    /** IANA timezone (e.g. {@code Asia/Kolkata}). Defaults to {@code Asia/Kolkata}. */
    @Builder.Default
    private String timezone = "Asia/Kolkata";

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
