package com.taskflow.backend.task;

import com.taskflow.backend.common.Priority;
import com.taskflow.backend.common.TaskStatus;

import java.time.Instant;

/**
 * Immutable set of optional filters for listing tasks (see {@code docs/API_SPEC.md}
 * §4.1). {@code userId} is always required; every other field is optional and,
 * when {@code null}, is ignored.
 *
 * @param userId    owner scope (required)
 * @param status    filter by workflow status
 * @param priority  filter by priority
 * @param categoryId filter by category
 * @param dueAfter  only tasks with {@code dueDate >= dueAfter}
 * @param dueBefore only tasks with {@code dueDate <= dueBefore}
 * @param overdue   when {@code true}, only tasks past due and not {@code DONE}
 * @param search    keyword matched (case-insensitively) against title + description
 */
public record TaskSearchCriteria(
        String userId,
        TaskStatus status,
        Priority priority,
        String categoryId,
        Instant dueAfter,
        Instant dueBefore,
        Boolean overdue,
        String search
) {
}
