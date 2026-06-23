package com.taskflow.backend.task.dto;

import com.taskflow.backend.common.Priority;
import com.taskflow.backend.common.TaskStatus;

import java.time.Instant;
import java.util.List;

/**
 * Read-only view of a {@link com.taskflow.backend.task.Task} returned to clients —
 * see {@code docs/API_SPEC.md} §4.
 *
 * <p>Omits {@code userId} (ownership is server-side only) and adds the computed
 * {@code isOverdue} flag.
 *
 * @param id          task id
 * @param title       title
 * @param description details (may be {@code null})
 * @param status      workflow status
 * @param priority    priority
 * @param dueDate     deadline (may be {@code null})
 * @param completedAt set when {@code status == DONE}, else {@code null}
 * @param categoryId  owning category id (may be {@code null})
 * @param tags        free-text tags (may be {@code null})
 * @param isOverdue   {@code dueDate < now && status != DONE}
 * @param createdAt   creation timestamp
 * @param updatedAt   last-update timestamp
 */
public record TaskResponse(
        String id,
        String title,
        String description,
        TaskStatus status,
        Priority priority,
        Instant dueDate,
        Instant completedAt,
        String categoryId,
        List<String> tags,
        boolean isOverdue,
        Instant createdAt,
        Instant updatedAt
) {
}
