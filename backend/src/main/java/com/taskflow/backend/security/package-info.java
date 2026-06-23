/**
 * Authentication and authorization infrastructure (stateless JWT).
 *
 * <p>This package owns the security plumbing that sits in front of the
 * controller layer. It is intentionally empty for now — no JWT or
 * authentication logic is implemented in this phase.
 *
 * <p>TODO (future phases):
 * <ul>
 *   <li>{@code JwtService} — issue and validate signed access/refresh tokens
 *       (claims: {@code sub=userId}, {@code role}, {@code exp}).</li>
 *   <li>{@code JwtAuthFilter} — a {@code OncePerRequestFilter} that extracts the
 *       bearer token, validates it, and populates the {@code SecurityContext}.</li>
 *   <li>{@code UserDetails} implementation and a {@code UserDetailsService} that
 *       loads the principal from the {@code users} collection.</li>
 *   <li>{@code AuthEntryPoint} — returns {@code 401} for missing/invalid tokens.</li>
 *   <li>A helper to resolve the authenticated {@code userId} for the service layer.</li>
 * </ul>
 */
package com.taskflow.backend.security;
