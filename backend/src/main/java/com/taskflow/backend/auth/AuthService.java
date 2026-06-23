package com.taskflow.backend.auth;

import com.taskflow.backend.auth.dto.AuthResponse;
import com.taskflow.backend.auth.dto.LoginRequest;
import com.taskflow.backend.auth.dto.RefreshRequest;
import com.taskflow.backend.auth.dto.RegisterRequest;
import com.taskflow.backend.common.DuplicateEmailException;
import com.taskflow.backend.common.InvalidCredentialsException;
import com.taskflow.backend.common.InvalidTokenException;
import com.taskflow.backend.security.JwtService;
import com.taskflow.backend.user.User;
import com.taskflow.backend.user.UserMapper;
import com.taskflow.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Business logic for registration, login, and token lifecycle.
 *
 * <p>Accounts are created with a BCrypt-hashed password; successful
 * register/login issues a stateless JWT pair (access + refresh) via
 * {@link JwtService}. Refresh exchanges a valid refresh token for a new access
 * token. Logout is a client-side discard in this phase (no server-side denylist).
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtService jwtService;

    /**
     * Registers a new account and immediately issues a token pair.
     *
     * <p>Normalises the email, rejects duplicates, hashes the password with BCrypt,
     * and persists the user with the default {@code USER} role.
     *
     * @param request validated registration payload
     * @return access + refresh tokens and the created user's safe profile
     * @throws DuplicateEmailException if an account with this email already exists
     */
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("An account with this email already exists");
        }

        User user = User.builder()
                .name(request.name().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                // role + timezone fall back to their document-level defaults (USER, Asia/Kolkata)
                .build();

        User saved = userRepository.save(user);
        return issueTokens(saved);
    }

    /**
     * Authenticates a user by email + password and issues a token pair.
     *
     * @param request validated login payload
     * @return access + refresh tokens and the authenticated profile
     * @throws InvalidCredentialsException if the email is unknown or the password is wrong
     */
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return issueTokens(user);
    }

    /**
     * Exchanges a valid refresh token for a fresh access token (§2.3).
     *
     * @param request the refresh-token payload
     * @return a slim {@link AuthResponse} carrying only the new access token
     * @throws InvalidTokenException if the token is missing/invalid/expired, is not a
     *                               refresh token, or its user no longer exists
     */
    public AuthResponse refresh(RefreshRequest request) {
        String token = request.refreshToken();

        if (!jwtService.isRefreshToken(token)) {
            throw new InvalidTokenException("Invalid or expired refresh token");
        }

        String userId = jwtService.extractUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired refresh token"));

        String accessToken = jwtService.generateAccessToken(user);
        return AuthResponse.accessOnly(accessToken, jwtService.getAccessTokenExpiresInSeconds());
    }

    /**
     * Logs the caller out (§2.4).
     *
     * <p>Tokens are stateless and not denylisted in this phase, so logout is a
     * no-op server-side — the client discards its stored tokens. The hook is kept
     * here so a refresh-token denylist can be added without touching the controller.
     */
    public void logout(RefreshRequest request) {
        // TODO(denylist): persist the refresh token jti until expiry to hard-revoke it.
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return AuthResponse.of(
                accessToken,
                refreshToken,
                jwtService.getAccessTokenExpiresInSeconds(),
                userMapper.toResponse(user));
    }

    /** Emails are stored and compared lowercased/trimmed (see {@code docs/DATABASE.md} §2). */
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
