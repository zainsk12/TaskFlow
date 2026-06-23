package com.taskflow.backend.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Strongly-typed binding for the {@code jwt.*} settings in
 * {@code application.properties} (see {@code docs/ARCHITECTURE.md}).
 *
 * <p>Registered via {@code @EnableConfigurationProperties(JwtProperties.class)} on
 * {@link com.taskflow.backend.config.SecurityConfig}.
 *
 * @param secret                 HS256 signing key. Must be at least 256 bits
 *                               (32 bytes / 32 ASCII chars); a longer secret is
 *                               recommended. Supplied via the {@code JWT_SECRET}
 *                               environment variable in deployed environments.
 * @param accessTokenExpiration  lifetime of short-lived access tokens (e.g. {@code 15m}).
 * @param refreshTokenExpiration lifetime of longer-lived refresh tokens (e.g. {@code 7d}).
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        Duration accessTokenExpiration,
        Duration refreshTokenExpiration
) {
}
