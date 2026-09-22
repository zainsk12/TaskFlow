package com.taskflow.backend.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Strongly-typed binding for the {@code app.refresh-cookie.*} settings in
 * {@code application.properties}.
 *
 * <p>These attributes vary by deployment topology: locally the Vite dev server
 * proxies {@code /api} to the backend, so the browser sees a same-origin
 * request and {@code Lax}/non-secure is sufficient. In production the frontend
 * (Vercel) and backend (Render) are different origins, so the cookie must be
 * {@code Secure} with {@code SameSite=None} to be sent cross-site at all.
 * Registered via {@code @EnableConfigurationProperties} on
 * {@link com.taskflow.backend.config.SecurityConfig}.
 *
 * @param secure   whether the cookie requires HTTPS. Supplied via the
 *                 {@code COOKIE_SECURE} environment variable in deployed
 *                 environments; defaults to {@code false} for local HTTP dev.
 * @param sameSite the {@code SameSite} attribute ({@code Lax}, {@code Strict}, or
 *                 {@code None}). Supplied via {@code COOKIE_SAME_SITE};
 *                 defaults to {@code Lax} for local dev. {@code None} requires
 *                 {@code secure=true} per spec.
 */
@ConfigurationProperties(prefix = "app.refresh-cookie")
public record RefreshCookieProperties(
        boolean secure,
        String sameSite
) {
}
