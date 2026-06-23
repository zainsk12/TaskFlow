package com.taskflow.backend.category.dto;

import java.time.Instant;

/**
 * Read-only view of a {@link com.taskflow.backend.category.Category} returned to
 * clients — see {@code docs/API_SPEC.md} §5.
 *
 * <p>Omits {@code userId} (ownership is server-side only) and adds the computed
 * {@code taskCount} (the number of the user's tasks in this category).
 *
 * @param id        the category id
 * @param name      label
 * @param color     hex colour
 * @param taskCount number of the user's tasks assigned to this category
 * @param createdAt creation timestamp
 * @param updatedAt last-update timestamp
 */
public record CategoryResponse(
        String id,
        String name,
        String color,
        long taskCount,
        Instant createdAt,
        Instant updatedAt
) {
}
