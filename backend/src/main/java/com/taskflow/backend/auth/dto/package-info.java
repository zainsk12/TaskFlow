/**
 * Request and response DTOs for the authentication module.
 *
 * <p>DTOs carry credentials and tokens across the wire; they never expose the
 * {@code passwordHash}. Defined here (see {@code docs/API_SPEC.md} §2):
 * <ul>
 *   <li>{@code RegisterRequest} — name, email, password (validated).</li>
 *   <li>{@code LoginRequest} — email, password (validated).</li>
 *   <li>{@code AuthResponse} — accessToken, refreshToken, tokenType, expiresIn, safe user view
 *       (token fields are {@code null} until the JWT phase).</li>
 * </ul>
 *
 * <p>TODO (JWT phase): {@code RefreshTokenRequest} / {@code LogoutRequest} (refreshToken)
 * and a {@code TokenResponse} (accessToken, tokenType, expiresIn) for refresh.
 */
package com.taskflow.backend.auth.dto;
