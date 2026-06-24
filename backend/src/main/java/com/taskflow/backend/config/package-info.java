/**
 * Application-wide Spring configuration.
 *
 * <p>This package holds {@code @Configuration} classes that wire up
 * cross-cutting infrastructure. Nothing here contains business logic.
 *
 * <p>Currently holds {@code MongoConfig} ({@code @EnableMongoAuditing}),
 * {@code PasswordConfig} (the {@code BCryptPasswordEncoder} bean),
 * {@code SecurityConfig} (stateless JWT {@code SecurityFilterChain}), and
 * {@code CorsConfig} (origin allow-list driven by {@code CORS_ALLOWED_ORIGINS}).
 *
 * <p>TODO (future phases):
 * <ul>
 *   <li>{@code OpenApiConfig} — Swagger / OpenAPI documentation metadata.</li>
 * </ul>
 */
package com.taskflow.backend.config;
