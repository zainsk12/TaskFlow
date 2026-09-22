package com.taskflow.backend.config;

import com.taskflow.backend.ratelimit.RateLimitFilter;
import com.taskflow.backend.ratelimit.RateLimitProperties;
import com.taskflow.backend.security.JwtAuthenticationEntryPoint;
import com.taskflow.backend.security.JwtAuthenticationFilter;
import com.taskflow.backend.security.JwtProperties;
import com.taskflow.backend.security.RefreshCookieProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Stateless JWT security wiring — the real replacement for the temporary
 * {@code spring.autoconfigure.exclude} hack used before this phase.
 *
 * <p>Policy:
 * <ul>
 *   <li>No sessions ({@link SessionCreationPolicy#STATELESS}); no HTTP Basic /
 *       form login. CSRF remains disabled: API authorization is still a Bearer
 *       access token attached manually by the frontend (not an ambient
 *       credential), so it is not CSRF-exploitable. The refresh-token cookie
 *       (see {@code AuthController}) is only read by {@code /auth/refresh} and
 *       {@code /auth/logout}; a forged cross-site request to either cannot read
 *       the response body thanks to the strict {@code CorsConfig} origin
 *       allow-list.</li>
 *   <li>{@code /api/v1/auth/**} is public (register, login, refresh, logout).</li>
 *   <li>Everything else — {@code /api/v1/users/**}, {@code /tasks/**},
 *       {@code /categories/**}, {@code /dashboard/**} — requires a valid access token.</li>
 *   <li>{@link RateLimitFilter} runs first, ahead of everything else, and throttles
 *       {@code /api/v1/auth/{login,register,refresh}} (Issue #3).</li>
 *   <li>{@link JwtAuthenticationFilter} runs before the username/password filter and
 *       populates the security context; {@link JwtAuthenticationEntryPoint} renders
 *       {@code 401}s as the standard {@code ApiError} body.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties({JwtProperties.class, RefreshCookieProperties.class, RateLimitProperties.class})
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final RateLimitFilter rateLimitFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public auth endpoints.
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // Public health check (used by Render's health probe).
                        .requestMatchers(HttpMethod.GET, "/health").permitAll()
                        // CORS preflight.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Protected API surface.
                        .requestMatchers(
                                "/api/v1/users/**",
                                "/api/v1/tasks/**",
                                "/api/v1/categories/**",
                                "/api/v1/dashboard/**"
                        ).authenticated()
                        .anyRequest().authenticated())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(rateLimitFilter, JwtAuthenticationFilter.class);

        return http.build();
    }
}
