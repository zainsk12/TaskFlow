package com.taskflow.backend.task;

import com.taskflow.backend.common.TaskStatus;
import com.taskflow.backend.task.dto.TaskResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Explicit mapper between the {@link Task} document and its wire DTO.
 *
 * <p>Computes the derived {@code isOverdue} flag at read time (never stored) and
 * omits {@code userId}/{@code position} from the response.
 */
@Component
public class TaskMapper {

    /** Maps a task to its response view, computing {@code isOverdue} against now. */
    public TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getCompletedAt(),
                task.getCategoryId(),
                task.getTags(),
                isOverdue(task),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }

    private boolean isOverdue(Task task) {
        return task.getDueDate() != null
                && task.getStatus() != TaskStatus.DONE
                && task.getDueDate().isBefore(Instant.now());
    }
}
