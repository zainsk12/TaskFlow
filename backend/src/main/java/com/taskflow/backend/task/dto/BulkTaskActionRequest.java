package com.taskflow.backend.task.dto;

import com.taskflow.backend.common.TaskStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request body for bulk task actions.
 *
 * <p>Currently supports updating the status of multiple tasks.
 */
public record BulkTaskActionRequest(

        @NotEmpty(message = "taskIds must not be empty")
        List<String> taskIds,

        @NotNull(message = "action is required")
        BulkTaskAction action,

        TaskStatus status

) {
}
