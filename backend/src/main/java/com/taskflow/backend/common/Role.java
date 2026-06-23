package com.taskflow.backend.common;

/**
 * Application security role assigned to a {@code User}.
 *
 * <p>Drives authorization decisions: a {@code USER} may only access their own
 * resources, while an {@code ADMIN} additionally has access to platform-level
 * metrics and account management (admin UI is a future feature).
 *
 * <p>Persisted as a String in the {@code users} collection and carried as the
 * {@code role} claim inside the JWT.
 *
 * <p>TODO: reference from the {@code User} document and from JWT claim mapping
 * once security is implemented.
 */
public enum Role {

    /** Default role for every registered account. Owns their own tasks/categories. */
    USER,

    /** Platform operator. All USER permissions plus aggregate metrics and account management. */
    ADMIN
}
