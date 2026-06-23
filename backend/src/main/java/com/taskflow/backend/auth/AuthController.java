package com.taskflow.backend.auth;

import com.taskflow.backend.auth.dto.AuthResponse;
import com.taskflow.backend.auth.dto.LoginRequest;
import com.taskflow.backend.auth.dto.RegisterRequest;
import com.taskflow.backend.user.dto.UserResponse;
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
 * {@link AuthService}. See {@code docs/API_SPEC.md} §2.
 *
 * <p>TODO (JWT phase): {@code POST /auth/refresh} and {@code POST /auth/logout}.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * {@code POST /api/v1/auth/register} — create an account. Returns
     * {@code 201 Created} with the new user's profile.
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    /**
     * {@code POST /api/v1/auth/login} — verify credentials. Returns {@code 200 OK}
     * with the authenticated profile (tokens are added in the JWT phase).
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
