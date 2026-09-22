package com.taskflow.backend.auth;

import com.taskflow.backend.auth.dto.AuthResponse;
import com.taskflow.backend.auth.dto.RefreshRequest;
import com.taskflow.backend.common.InvalidTokenException;
import com.taskflow.backend.common.Role;
import com.taskflow.backend.security.JwtProperties;
import com.taskflow.backend.security.JwtService;
import com.taskflow.backend.security.RevokedToken;
import com.taskflow.backend.security.RevokedTokenRepository;
import com.taskflow.backend.user.User;
import com.taskflow.backend.user.UserMapper;
import com.taskflow.backend.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AuthService}'s refresh-token revocation behaviour
 * (Issue #1: logout revokes the refresh token; a revoked token is rejected by
 * refresh; an unrevoked token keeps working).
 *
 * <p>Uses a real {@link JwtService} (built from a throwaway test-only
 * {@link JwtProperties}, not a real secret) so the {@code jti} claims exercised
 * here are genuine; every persistence dependency ({@link UserRepository},
 * {@link RevokedTokenRepository}) is mocked so no MongoDB instance is needed.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private RevokedTokenRepository revokedTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private JwtService jwtService;
    private AuthService authService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(new JwtProperties(
                "unit-test-only-signing-key-not-a-real-secret-0000",
                Duration.ofMinutes(15),
                Duration.ofDays(7)));
        authService = new AuthService(userRepository, passwordEncoder, userMapper, jwtService, revokedTokenRepository);

        user = User.builder()
                .id("user-1")
                .name("Test User")
                .email("test@example.com")
                .passwordHash("hashed")
                .role(Role.USER)
                .build();
    }

    @Test
    void refreshSucceedsForAValidNonRevokedToken() {
        String refreshToken = jwtService.generateRefreshToken(user);
        when(revokedTokenRepository.existsByJti(any())).thenReturn(false);
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        AuthResponse response = authService.refresh(new RefreshRequest(refreshToken));

        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNull();
    }

    @Test
    void refreshRejectsARevokedToken() {
        String refreshToken = jwtService.generateRefreshToken(user);
        when(revokedTokenRepository.existsByJti(any())).thenReturn(true);

        assertThatThrownBy(() -> authService.refresh(new RefreshRequest(refreshToken)))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void logoutRevokesThePresentedRefreshToken() {
        String refreshToken = jwtService.generateRefreshToken(user);
        when(revokedTokenRepository.existsByJti(any())).thenReturn(false);

        authService.logout(new RefreshRequest(refreshToken));

        ArgumentCaptor<RevokedToken> captor = ArgumentCaptor.forClass(RevokedToken.class);
        verify(revokedTokenRepository).save(captor.capture());
        RevokedToken saved = captor.getValue();
        assertThat(saved.getJti()).isEqualTo(jwtService.extractJti(refreshToken));
        assertThat(saved.getUserId()).isEqualTo("user-1");
        assertThat(saved.getExpiresAt()).isAfter(Instant.now());
    }

    @Test
    void logoutIsANoOpForAnAlreadyRevokedToken() {
        String refreshToken = jwtService.generateRefreshToken(user);
        when(revokedTokenRepository.existsByJti(any())).thenReturn(true);

        authService.logout(new RefreshRequest(refreshToken));

        verify(revokedTokenRepository, never()).save(any());
    }

    @Test
    void logoutIsANoOpForANonRefreshToken() {
        // An access token is a validly-signed token, but the wrong `typ` — logout
        // must not revoke or error on it, just do nothing.
        String accessToken = jwtService.generateAccessToken(user);

        authService.logout(new RefreshRequest(accessToken));

        verify(revokedTokenRepository, never()).existsByJti(any());
        verify(revokedTokenRepository, never()).save(any());
    }
}
