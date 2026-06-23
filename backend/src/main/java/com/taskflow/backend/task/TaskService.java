package com.taskflow.backend.task;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Business logic for task management and the workflow lifecycle.
 *
 * <p>This is where ownership is enforced (does <em>this</em> user own
 * <em>this</em> task?) and where workflow rules live. Skeleton only — no logic
 * is implemented in this phase.
 *
 * <p>TODO (future phases):
 * <ul>
 *   <li>CRUD scoped by the authenticated {@code userId}.</li>
 *   <li>Apply defaults on create (status=TODO, priority=MEDIUM).</li>
 *   <li>Validate that a supplied {@code categoryId} exists and is owned by the same user.</li>
 *   <li>Status transitions: set/clear {@code completedAt} on entering/leaving DONE.</li>
 *   <li>Filtering, sorting, keyword search, and pagination.</li>
 *   <li>Compute {@code isOverdue} at read time when mapping to the response DTO.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
}
