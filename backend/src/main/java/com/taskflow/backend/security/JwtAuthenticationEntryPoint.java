package com.taskflow.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

/**
 * Renders a {@code 401 Unauthorized} as the standard
 * {@link com.taskflow.backend.common.ApiError} body when a request to a protected
 * endpoint arrives without valid authentication (missing, malformed, or expired
 * token).
 *
 * <p>Keeps unauthenticated responses consistent with
 * {@link com.taskflow.backend.common.GlobalExceptionHandler} — the handler does
 * not see these because Spring Security rejects the request before it reaches a
 * controller. The body is written by hand (a fixed, field-free error) so this
 * filter-stage component carries no Jackson {@code ObjectMapper} dependency.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String body = "{"
                + "\"timestamp\":\"" + Instant.now() + "\","
                + "\"status\":401,"
                + "\"error\":\"" + HttpStatus.UNAUTHORIZED.getReasonPhrase() + "\","
                + "\"message\":\"Authentication required\","
                + "\"path\":\"" + jsonEscape(request.getRequestURI()) + "\""
                + "}";

        response.getWriter().write(body);
    }

    /** Minimal JSON string escaping for the request path. */
    private String jsonEscape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
