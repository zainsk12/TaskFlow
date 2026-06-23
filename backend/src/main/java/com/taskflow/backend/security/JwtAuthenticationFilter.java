package com.taskflow.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Authenticates requests that carry a valid {@code Authorization: Bearer <jwt>}
 * access token.
 *
 * <p>Runs once per request. When a valid access token is present it builds an
 * {@link UsernamePasswordAuthenticationToken} (principal = user id, one
 * {@code ROLE_*} authority from the token's {@code role} claim) and stores it in
 * the {@link SecurityContextHolder}. Missing, malformed, expired, or non-access
 * tokens are simply ignored — the request proceeds unauthenticated and the
 * authorization rules in {@code SecurityConfig} reject it with {@code 401} via
 * {@link JwtAuthenticationEntryPoint}.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveBearerToken(request);

        if (token != null
                && SecurityContextHolder.getContext().getAuthentication() == null
                && jwtService.isAccessToken(token)) {

            String userId = jwtService.extractUserId(token);
            String role = jwtService.extractRole(token);
            List<SimpleGrantedAuthority> authorities =
                    List.of(new SimpleGrantedAuthority("ROLE_" + role));

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    /** Extracts the raw token from the {@code Authorization} header, or {@code null}. */
    private String resolveBearerToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length()).trim();
        }
        return null;
    }
}
