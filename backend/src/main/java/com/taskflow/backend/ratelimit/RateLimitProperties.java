package com.taskflow.backend.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Strongly-typed binding for the {@code app.rate-limit.*} settings in
 * {@code application.properties} (Issue #3).
 *
 * <p>Registered via {@code @EnableConfigurationProperties} on
 * {@link com.taskflow.backend.config.SecurityConfig}. All three auth endpoints
 * share one time window but get independently configurable request ceilings,
 * since login/refresh traffic patterns differ from registration.
 *
 * @param enabled              master on/off switch; set to {@code false} to
 *                             disable rate limiting entirely (e.g. local dev,
 *                             load testing).
 * @param window               the sliding time window each limit is measured over
 *                             (e.g. {@code 1m}).
 * @param loginMaxRequests     max {@code POST /api/v1/auth/login} requests per
 *                             key per window.
 * @param registerMaxRequests  max {@code POST /api/v1/auth/register} requests
 *                             per key per window.
 * @param refreshMaxRequests   max {@code POST /api/v1/auth/refresh} requests
 *                             per key per window.
 */
@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProperties(
        boolean enabled,
        Duration window,
        int loginMaxRequests,
        int registerMaxRequests,
        int refreshMaxRequests
) {
}
