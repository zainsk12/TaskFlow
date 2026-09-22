package com.taskflow.backend.auth;

import com.taskflow.backend.auth.dto.AuthResponse;
import com.taskflow.backend.auth.dto.LoginRequest;
import com.taskflow.backend.auth.dto.RefreshRequest;
import com.taskflow.backend.auth.dto.RegisterRequest;
import com.taskflow.backend.common.InvalidTokenException;
import com.taskflow.backend.security.JwtService;
import com.taskflow.backend.security.RefreshCookieProperties;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication ({@code /api/v1/auth}).
 *
 * <p>Thin HTTP adapter: validates request DTOs ({@code @Valid}) and delegates to
 * {@link AuthService}. All routes here are public (see {@code SecurityConfig});
 * see {@code docs/API_SPEC.md} §2.
 *
 * <p>The refresh token is never exposed to JavaScript: register/login set it as
 * an HttpOnly cookie (and strip it from the JSON body), {@code /refresh} and
 * {@code /logout} read it back from that cookie, and {@code /logout} clears it.
 * {@link AuthService}'s revocation logic (Issue #1) is unchanged — it still
 * operates on the raw token string, just sourced from the cookie now instead of
 * a request body.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    /** Name of the HttpOnly cookie carrying the refresh token. */
    private static final String REFRESH_COOKIE_NAME = "refreshToken";

    /** Scope the cookie to the auth endpoints that actually need it. */
    private static final String REFRESH_COOKIE_PATH = "/api/v1/auth";

    private final AuthService authService;
    private final JwtService jwtService;
    private final RefreshCookieProperties cookieProperties;

    /**
     * {@code POST /api/v1/auth/register} — create an account. Returns
     * {@code 201 Created} with an access token and the new profile; the refresh
     * token is set as an HttpOnly cookie, not returned in the body.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(response.refreshToken()).toString())
                .body(response.withoutRefreshToken());
    }

    /**
     * {@code POST /api/v1/auth/login} — verify credentials. Returns {@code 200 OK}
     * with an access token and the authenticated profile; the refresh token is
     * set as an HttpOnly cookie, not returned in the body.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(response.refreshToken()).toString())
                .body(response.withoutRefreshToken());
    }

    /**
     * {@code POST /api/v1/auth/refresh} — exchange the refresh-token cookie for a
     * new access token. Returns {@code 200 OK}; {@code 401} if the cookie is
     * missing, invalid, expired, or revoked.
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = REFRESH_COOKIE_NAME, required = false) String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidTokenException("Invalid or expired refresh token");
        }
        return ResponseEntity.ok(authService.refresh(new RefreshRequest(refreshToken)));
    }

    /**
     * {@code POST /api/v1/auth/logout} — revoke and clear the caller's
     * refresh-token cookie. Returns {@code 204 No Content}.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = REFRESH_COOKIE_NAME, required = false) String refreshToken) {
        authService.logout(new RefreshRequest(refreshToken));
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString())
                .build();
    }

    /** Builds the {@code Set-Cookie} value that stores a fresh refresh token. */
    private ResponseCookie buildRefreshCookie(String refreshToken) {
        return ResponseCookie.from(REFRESH_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(cookieProperties.secure())
                .sameSite(cookieProperties.sameSite())
                .path(REFRESH_COOKIE_PATH)
                .maxAge(jwtService.getRefreshTokenExpiresInSeconds())
                .build();
    }

    /** Builds the {@code Set-Cookie} value that expires/clears the refresh-token cookie. */
    private ResponseCookie clearRefreshCookie() {
        return ResponseCookie.from(REFRESH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(cookieProperties.secure())
                .sameSite(cookieProperties.sameSite())
                .path(REFRESH_COOKIE_PATH)
                .maxAge(0)
                .build();
    }
}
