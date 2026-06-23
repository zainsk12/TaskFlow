package com.taskflow.backend.task.dto;

import com.taskflow.backend.common.Priority;
import com.taskflow.backend.common.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

/**
 * Request body for {@code POST /api/v1/tasks} — see {@code docs/API_SPEC.md} §4.2.
 *
 * <p>{@code userId} is never accepted from the client; it comes from the token.
 * {@code status} defaults to {@code TODO} and {@code priority} to {@code MEDIUM}
 * when omitted. Unknown {@code status}/{@code priority} values are rejected with
 * {@code 400} during JSON binding.
 *
 * @param title       required, 1–120 chars
 * @param description optional, ≤ 2000 chars
 * @param status      optional workflow status (default {@code TODO})
 * @param priority    optional priority (default {@code MEDIUM})
 * @param dueDate     optional ISO-8601 deadline
 * @param categoryId  optional; must reference a category owned by the same user
 * @param tags        optional free-text tags
 */
public record CreateTaskRequest(

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
