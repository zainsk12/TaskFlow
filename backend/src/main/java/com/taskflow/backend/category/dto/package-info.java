/**
 * Request and response DTOs for the category module.
 *
 * <p>DTOs decouple the wire contract from the {@code Category} persistence model
 * and ensure {@code userId} is never accepted from the client. See
 * {@code docs/API_SPEC.md} §5.
 *
 * <ul>
 *   <li>{@code CreateCategoryRequest} — name (1–40), color (hex), validated.</li>
 *   <li>{@code UpdateCategoryRequest} — name, color (full update).</li>
 *   <li>{@code CategoryResponse} — id, name, color, computed {@code taskCount}, timestamps.</li>
 * </ul>
 */
package com.taskflow.backend.category.dto;
