package com.taskflow.backend.task.dto;

import com.taskflow.backend.common.Priority;
import com.taskflow.backend.common.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

/**
 * Request body for {@code PUT /api/v1/tasks/{id}} — see {@code docs/API_SPEC.md} §4.4.
 *
 * <p>Update semantics: {@code title} is required and always applied. The other
 * fields are applied only when present; an omitted ({@code null}) {@code status},
 * {@code priority}, or {@code tags} leaves the stored value unchanged, while
 * {@code description}, {@code dueDate}, and {@code categoryId} are replaced as
 * given (sending {@code null} clears them). Transitioning {@code status} to/from
 * {@code DONE} sets/clears {@code completedAt} server-side.
 *
 * @param title       required, 1–120 chars
 * @param description optional, ≤ 2000 chars
 * @param status      optional workflow status (unchanged if omitted)
 * @param priority    optional priority (unchanged if omitted)
 * @param dueDate     optional ISO-8601 deadline (cleared if {@code null})
 * @param categoryId  optional; must reference a category owned by the same user (cleared if {@code null})
 * @param tags        optional free-text tags (unchanged if omitted)
 */
public record UpdateTaskRequest(

        @NotBlank(message = "title must not be blank")
        @Size(min = 1, max = 120, message = "title must be between 1 and 120 characters")
        String title,

        @Size(max = 2000, message = "description must be at most 2000 characters")
        String description,

        TaskStatus status,

        Priority priority,

        Instant dueDate,

        String categoryId,

        List<String> tags
) {
}
