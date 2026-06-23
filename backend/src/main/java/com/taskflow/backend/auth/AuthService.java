package com.taskflow.backend.auth;

import com.taskflow.backend.auth.dto.AuthResponse;
import com.taskflow.backend.auth.dto.LoginRequest;
import com.taskflow.backend.auth.dto.RegisterRequest;
import com.taskflow.backend.common.DuplicateEmailException;
import com.taskflow.backend.common.InvalidCredentialsException;
import com.taskflow.backend.user.User;
import com.taskflow.backend.user.UserMapper;
import com.taskflow.backend.user.UserRepository;
import com.taskflow.backend.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Business logic for registration and login.
 *
 * <p>This phase establishes the credential foundation only — accounts are created
 * with a BCrypt-hashed password and logins are verified against that hash.
 * <strong>No JWT is issued yet</strong>: {@link #login} returns the authenticated
 * profile inside a token-less {@link AuthResponse} (see {@code docs/API_SPEC.md} §2).
 *
 * <p>TODO (JWT phase):
 * <ul>
 *   <li>Issue access + refresh tokens on register/login and populate {@link AuthResponse}.</li>
 *   <li>{@code refresh(request)} and {@code logout(request)}.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    /**
     * Registers a new account.
     *
     * <p>Normalises the email, rejects duplicates, hashes the password with BCrypt,
     * and persists the user with the default {@code USER} role.
     *
     * @param request validated registration payload
     * @return the created user's safe profile (HTTP {@code 201})
     * @throws DuplicateEmailException if an account with this email already exists
     */
    public UserResponse register(RegisterRequest request) {
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
        return userMapper.toResponse(saved);
    }

    /**
     * Authenticates a user by email + password.
     *
     * @param request validated login payload
     * @return a token-less {@link AuthResponse} carrying the authenticated profile
     * @throws InvalidCredentialsException if the email is unknown or the password is wrong
     */
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // TODO(jwt): issue access + refresh tokens here and return them in the response.
        return AuthResponse.withoutTokens(userMapper.toResponse(user));
    }

    /** Emails are stored and compared lowercased/trimmed (see {@code docs/DATABASE.md} §2). */
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
