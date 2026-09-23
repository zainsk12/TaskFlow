package com.taskflow.backend.task.dto;

import com.taskflow.backend.common.TaskStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for {@code PATCH /api/v1/tasks/{id}/status} — see {@code docs/API_SPEC.md} §4.5.
 *
 * <p>Carries only the new workflow status so the client does not need to repeat
 * all other task fields. Unknown enum values are rejected with {@code 400} during
 * JSON deserialization; a missing ({@code null}) value is rejected by the
 * {@link NotNull} constraint.
 *
 * @param status the new workflow status; must be one of {@code TODO},
 *               {@code IN_PROGRESS}, or {@code DONE}
 */
public record UpdateTaskStatusRequest(

        @NotNull(message = "status must not be null")
        TaskStatus status
) {
}
