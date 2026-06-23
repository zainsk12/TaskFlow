package com.taskflow.backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

/**
 * Helpers for reading the authenticated principal out of the Spring
 * {@link SecurityContextHolder}.
 *
 * <p>{@link JwtAuthenticationFilter} stores the user id as the authentication
 * <em>principal name</em> (from the JWT {@code sub} claim), so the service layer
 * resolves "who is calling" through here instead of touching the security API
 * directly.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * Returns the id of the currently authenticated user.
     *
     * @throws ResponseStatusException {@code 401} if there is no authenticated
     *                                 principal in the context
     */
    public static String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
