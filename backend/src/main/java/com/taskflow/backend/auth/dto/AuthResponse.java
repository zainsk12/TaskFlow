package com.taskflow.backend.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.taskflow.backend.user.dto.UserResponse;

/**
 * Response body for the token-issuing auth endpoints — see {@code docs/API_SPEC.md}
 * §2.2 (login) / §2.3 (refresh).
 *
 * <p>{@code null} fields are omitted from the JSON ({@link JsonInclude}), so the
 * same record serves two shapes:
 * <ul>
 *   <li><b>full</b> ({@link #of}) — register/login: all fields, including
 *       {@code refreshToken} and the {@code user} profile.</li>
 *   <li><b>access-only</b> ({@link #accessOnly}) — refresh: just
 *       {@code accessToken}, {@code tokenType}, {@code expiresIn}.</li>
 * </ul>
 *
 * @param accessToken  short-lived JWT used as the {@code Bearer} credential
 * @param refreshToken longer-lived JWT exchanged at {@code /auth/refresh} (omitted on refresh)
 * @param tokenType    always {@code "Bearer"}
 * @param expiresIn    access-token lifetime in seconds
 * @param user         the authenticated user's safe profile view (omitted on refresh)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        UserResponse user
) {

    /** Token type used throughout the API. */
    public static final String BEARER = "Bearer";

    /** Full response for register/login: access + refresh tokens and the user profile. */
    public static AuthResponse of(String accessToken, String refreshToken, long expiresIn, UserResponse user) {
        return new AuthResponse(accessToken, refreshToken, BEARER, expiresIn, user);
    }

    /** Slim response for token refresh: a fresh access token only (§2.3). */
    public static AuthResponse accessOnly(String accessToken, long expiresIn) {
        return new AuthResponse(accessToken, null, BEARER, expiresIn, null);
    }
}
