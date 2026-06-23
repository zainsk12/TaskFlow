/**
 * Authentication and authorization infrastructure (stateless JWT).
 *
 * <p>This package owns the stateless-JWT plumbing that sits in front of the
 * controller layer:
 * <ul>
 *   <li>{@link com.taskflow.backend.security.JwtService} — issues and validates
 *       HS256 access/refresh tokens (claims: {@code sub=userId}, {@code email},
 *       {@code role}, {@code typ}).</li>
 *   <li>{@link com.taskflow.backend.security.JwtAuthenticationFilter} — a
 *       {@code OncePerRequestFilter} that extracts the bearer token, validates it,
 *       and populates the {@code SecurityContext}.</li>
 *   <li>{@link com.taskflow.backend.security.JwtAuthenticationEntryPoint} — returns
 *       {@code 401} (as the standard {@code ApiError}) for missing/invalid tokens.</li>
 *   <li>{@link com.taskflow.backend.security.SecurityUtils} — resolves the
 *       authenticated {@code userId} for the service layer.</li>
 *   <li>{@link com.taskflow.backend.security.JwtProperties} — binds {@code jwt.*}.</li>
 * </ul>
 *
 * <p>The {@code SecurityFilterChain} itself lives in
 * {@link com.taskflow.backend.config.SecurityConfig}.
 */
package com.taskflow.backend.security;
