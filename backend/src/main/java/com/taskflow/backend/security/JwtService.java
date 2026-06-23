package com.taskflow.backend.security;

import com.taskflow.backend.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

/**
 * Issues and verifies the application's JSON Web Tokens.
 *
 * <p>All tokens are signed with <strong>HMAC-SHA256 (HS256)</strong> using the
 * shared secret from {@link JwtProperties}. Two kinds are produced, distinguished
 * by the custom {@code typ} claim:
 *
 * <ul>
 *   <li><b>access</b> — short-lived, sent on every request as a {@code Bearer}
 *       token and validated by {@link JwtAuthenticationFilter}.</li>
 *   <li><b>refresh</b> — longer-lived, exchanged at {@code /api/v1/auth/refresh}
 *       for a fresh access token.</li>
 * </ul>
 *
 * <p>Claims layout: {@code sub} = user id, {@code email}, {@code role}, {@code typ}.
 */
@Service
public class JwtService {

    /** Custom claim name carrying the token kind ("access" / "refresh"). */
    static final String CLAIM_TOKEN_TYPE = "typ";
    /** Custom claim name carrying the user's email. */
    static final String CLAIM_EMAIL = "email";
    /** Custom claim name carrying the user's role. */
    static final String CLAIM_ROLE = "role";

    static final String TYPE_ACCESS = "access";
    static final String TYPE_REFRESH = "refresh";

    private final SecretKey signingKey;
    private final Duration accessTokenExpiration;
    private final Duration refreshTokenExpiration;

    public JwtService(JwtProperties properties) {
        byte[] keyBytes = properties.secret().getBytes(StandardCharsets.UTF_8);
        // Keys.hmacShaKeyFor enforces the >=256-bit minimum required for HS256.
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpiration = properties.accessTokenExpiration();
        this.refreshTokenExpiration = properties.refreshTokenExpiration();
    }

    // ------------------------------------------------------------------
    // Generation
    // ------------------------------------------------------------------

    /** Issues a short-lived access token for the given user. */
    public String generateAccessToken(User user) {
        return buildToken(user, TYPE_ACCESS, accessTokenExpiration);
    }

    /** Issues a longer-lived refresh token for the given user. */
    public String generateRefreshToken(User user) {
        return buildToken(user, TYPE_REFRESH, refreshTokenExpiration);
    }

    private String buildToken(User user, String tokenType, Duration ttl) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + ttl.toMillis());
        return Jwts.builder()
                .subject(user.getId())
                .claim(CLAIM_EMAIL, user.getEmail())
                .claim(CLAIM_ROLE, user.getRole().name())
                .claim(CLAIM_TOKEN_TYPE, tokenType)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    // ------------------------------------------------------------------
    // Validation / extraction
    // ------------------------------------------------------------------

    /**
     * Returns {@code true} if the token has a valid signature and is not expired.
     * A malformed, tampered, or expired token yields {@code false} (never throws).
     */
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    /** {@code true} if the token is a valid, non-expired <em>access</em> token. */
    public boolean isAccessToken(String token) {
        return isTokenOfType(token, TYPE_ACCESS);
    }

    /** {@code true} if the token is a valid, non-expired <em>refresh</em> token. */
    public boolean isRefreshToken(String token) {
        return isTokenOfType(token, TYPE_REFRESH);
    }

    private boolean isTokenOfType(String token, String expectedType) {
        try {
            return expectedType.equals(parseClaims(token).get(CLAIM_TOKEN_TYPE, String.class));
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    /** Extracts the user id ({@code sub} claim). Throws if the token is invalid. */
    public String extractUserId(String token) {
        return parseClaims(token).getSubject();
    }

    /** Extracts the {@code email} claim. Throws if the token is invalid. */
    public String extractEmail(String token) {
        return parseClaims(token).get(CLAIM_EMAIL, String.class);
    }

    /** Extracts the {@code role} claim. Throws if the token is invalid. */
    public String extractRole(String token) {
        return parseClaims(token).get(CLAIM_ROLE, String.class);
    }

    /** {@code true} if the token's {@code exp} is in the past. Throws if otherwise invalid. */
    public boolean isExpired(String token) {
        try {
            return parseClaims(token).getExpiration().before(new Date());
        } catch (io.jsonwebtoken.ExpiredJwtException ex) {
            return true;
        }
    }

    /** Access-token lifetime in seconds — the {@code expiresIn} value returned to clients. */
    public long getAccessTokenExpiresInSeconds() {
        return accessTokenExpiration.toSeconds();
    }

    /**
     * Parses and verifies the token, returning its claims.
     *
     * @throws JwtException if the signature is invalid, the token is malformed,
     *                      or it has expired
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
