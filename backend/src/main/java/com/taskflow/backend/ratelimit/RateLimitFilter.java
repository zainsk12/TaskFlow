package com.taskflow.backend.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

/**
 * Rate-limits the authentication endpoints most exposed to brute-force,
 * credential-stuffing, and registration-spam traffic (Issue #3):
 * {@code POST /api/v1/auth/login}, {@code /register}, and {@code /refresh}.
 *
 * <p>{@code /logout} is intentionally excluded — it doesn't authenticate
 * anything (a bad token is already a silent no-op in {@code AuthService}), so
 * there's nothing for an attacker to gain by hammering it. Every other route is
 * passed straight through, so the rest of the API is not throttled by this
 * filter at all.
 *
 * <p>Requests are keyed by {@code clientIp:endpoint} (see {@link #clientIp}), so
 * one user's repeated bad attempts don't lock out others on the same network,
 * and separate limits per endpoint keep e.g. registration spam from also
 * blocking that IP's login attempts. Exceeding the limit returns
 * {@code 429 Too Many Requests} as the standard {@code ApiError} shape, with a
 * generic message — it never reveals whether the underlying email/account
 * exists, mirroring {@code InvalidCredentialsException}'s approach.
 *
 * <p>Registered ahead of {@link com.taskflow.backend.security.JwtAuthenticationFilter}
 * in {@code SecurityConfig} so an over-budget client is rejected before any JWT
 * parsing happens.
 */
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String AUTH_PREFIX = "/api/v1/auth/";

    private final RateLimiter rateLimiter;
    private final RateLimitProperties properties;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        Endpoint endpoint = properties.enabled() && HttpMethod.POST.matches(request.getMethod())
                ? resolveEndpoint(request.getRequestURI())
                : null;

        if (endpoint == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = clientIp(request) + ":" + endpoint.label;
        boolean allowed = rateLimiter.tryConsume(key, endpoint.maxRequests(properties), properties.window());

        if (!allowed) {
            writeTooManyRequests(response, request);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private Endpoint resolveEndpoint(String uri) {
        if (!uri.startsWith(AUTH_PREFIX)) {
            return null;
        }
        return switch (uri.substring(AUTH_PREFIX.length())) {
            case "login" -> Endpoint.LOGIN;
            case "register" -> Endpoint.REGISTER;
            case "refresh" -> Endpoint.REFRESH;
            default -> null;
        };
    }

    /**
     * Resolves the caller's IP. {@code request.getRemoteAddr()} reflects the real
     * client address (not Render's edge proxy) because
     * {@code server.forward-headers-strategy=native} is enabled, which makes the
     * embedded Tomcat trust and apply {@code X-Forwarded-For} from the immediate
     * upstream proxy. Falls back to a fixed key if the address is ever blank.
     */
    private String clientIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        return (remoteAddr == null || remoteAddr.isBlank()) ? "unknown" : remoteAddr;
    }

    /** Mirrors {@code JwtAuthenticationEntryPoint}: hand-written JSON, no ObjectMapper at filter stage. */
    private void writeTooManyRequests(HttpServletResponse response, HttpServletRequest request) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String body = "{"
                + "\"timestamp\":\"" + Instant.now() + "\","
                + "\"status\":429,"
                + "\"error\":\"" + HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase() + "\","
                + "\"message\":\"Too many requests. Please try again later.\","
                + "\"path\":\"" + jsonEscape(request.getRequestURI()) + "\""
                + "}";

        response.getWriter().write(body);
    }

    private String jsonEscape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private enum Endpoint {
        LOGIN("login"),
        REGISTER("register"),
        REFRESH("refresh");

        private final String label;

        Endpoint(String label) {
            this.label = label;
        }

        int maxRequests(RateLimitProperties properties) {
            return switch (this) {
                case LOGIN -> properties.loginMaxRequests();
                case REGISTER -> properties.registerMaxRequests();
                case REFRESH -> properties.refreshMaxRequests();
            };
        }
    }
}
