/**
 * Rate limiting for the authentication endpoints (Issue #3).
 *
 * <ul>
 *   <li>{@link com.taskflow.backend.ratelimit.RateLimiter} — the in-memory,
 *       per-instance fixed-window counter.</li>
 *   <li>{@link com.taskflow.backend.ratelimit.RateLimitFilter} — the
 *       {@code OncePerRequestFilter} that applies it to
 *       {@code /api/v1/auth/{login,register,refresh}}.</li>
 *   <li>{@link com.taskflow.backend.ratelimit.RateLimitProperties} — binds
 *       {@code app.rate-limit.*}.</li>
 * </ul>
 *
 * <p>Wired into the chain by {@code SecurityConfig}, ahead of
 * {@code JwtAuthenticationFilter}.
 */
package com.taskflow.backend.ratelimit;
