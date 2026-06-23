package com.taskflow.backend.auth;

import com.taskflow.backend.auth.dto.AuthResponse;
import com.taskflow.backend.auth.dto.LoginRequest;
import com.taskflow.backend.auth.dto.RefreshRequest;
import com.taskflow.backend.auth.dto.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * {@code POST /api/v1/auth/register} — create an account. Returns
     * {@code 201 Created} with a fresh access/refresh token pair and the new profile.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    /**
     * {@code POST /api/v1/auth/login} — verify credentials. Returns {@code 200 OK}
     * with an access/refresh token pair and the authenticated profile.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * {@code POST /api/v1/auth/refresh} — exchange a refresh token for a new
     * access token. Returns {@code 200 OK}; {@code 401} if the token is invalid/expired.
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    /**
     * {@code POST /api/v1/auth/logout} — discard the caller's refresh token.
     * Returns {@code 204 No Content}.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }
}
