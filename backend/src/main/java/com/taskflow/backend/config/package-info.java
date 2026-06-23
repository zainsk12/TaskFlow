/**
 * Application-wide Spring configuration.
 *
 * <p>This package holds {@code @Configuration} classes that wire up
 * cross-cutting infrastructure. Nothing here contains business logic.
 *
 * <p>Currently holds {@code MongoConfig} ({@code @EnableMongoAuditing}) and
 * {@code PasswordConfig} (the {@code BCryptPasswordEncoder} bean).
 *
 * <p>TODO (future phases):
 * <ul>
 *   <li>{@code SecurityConfig} — stateless {@code SecurityFilterChain}, registers
 *       the JWT auth filter, and public vs. protected route matchers. Adding it
 *       replaces the temporary security auto-config exclusion in
 *       {@code application.properties}.</li>
 *   <li>{@code CorsConfig} — restricts allowed origins to the deployed Vercel frontend
 *       (read from {@code CORS_ALLOWED_ORIGINS}).</li>
 *   <li>{@code OpenApiConfig} — Swagger / OpenAPI documentation metadata.</li>
 * </ul>
 */
package com.taskflow.backend.config;
