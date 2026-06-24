package com.taskflow.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS policy for the browser-based SPA frontend.
 *
 * <p>Allowed origins are environment-driven via {@code CORS_ALLOWED_ORIGINS}
 * (comma-separated, no trailing slash). Locally this defaults to the Vite dev
 * server ({@code http://localhost:5173}); in production it should be set to the
 * deployed Vercel origin(s). The {@link CorsConfigurationSource} bean is consumed
 * by {@code SecurityConfig} via {@code http.cors(...)} so the policy is enforced
 * inside the Spring Security filter chain (preflight {@code OPTIONS} is permitted
 * there as well).
 */
@Configuration
public class CorsConfig {

    private final List<String> allowedOrigins;

    public CorsConfig(
            @Value("${CORS_ALLOWED_ORIGINS:http://localhost:5173}") String allowedOrigins) {
        this.allowedOrigins = List.of(allowedOrigins.split("\\s*,\\s*"));
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        // Auth is stateless Bearer tokens in the Authorization header (not cookies),
        // so credentials are not required; keep them disabled to allow explicit origins.
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
