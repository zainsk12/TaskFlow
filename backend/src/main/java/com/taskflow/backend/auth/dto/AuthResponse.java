package com.taskflow.backend.auth.dto;

import com.taskflow.backend.user.dto.UserResponse;

/**
 * Response body for {@code POST /api/v1/auth/login} — see {@code docs/API_SPEC.md} §2.2.
 *
 * <p>The token-bearing shape is defined now so the wire contract is stable, but
 * <strong>JWT is not implemented in this phase</strong>: {@code accessToken},
 * {@code refreshToken} and {@code expiresIn} are {@code null} for now and will be
 * populated once {@code JwtService} exists. {@code tokenType} is always
 * {@code "Bearer"}; {@code user} carries the authenticated profile.
 *
 * @param accessToken  short-lived JWT — {@code null} until the JWT phase
 * @param refreshToken longer-lived JWT — {@code null} until the JWT phase
 * @param tokenType    always {@code "Bearer"}
 * @param expiresIn    access-token lifetime in seconds — {@code null} until the JWT phase
 * @param user         the authenticated user's safe profile view
 */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        UserResponse user
) {

    /** Token type used throughout the API. */
    public static final String BEARER = "Bearer";

    /**
     * Builds a login response carrying only the authenticated profile.
     *
     * <p>TODO(jwt): replace with a variant that also sets {@code accessToken},
     * {@code refreshToken} and {@code expiresIn} once tokens are issued.
     */
    public static AuthResponse withoutTokens(UserResponse user) {
        return new AuthResponse(null, null, BEARER, null, user);
    }
}
