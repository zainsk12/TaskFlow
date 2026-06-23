package com.taskflow.backend.task;

import com.taskflow.backend.category.CategoryRepository;
import com.taskflow.backend.common.PageResponse;
import com.taskflow.backend.common.Priority;
import com.taskflow.backend.common.ResourceNotFoundException;
import com.taskflow.backend.common.TaskStatus;
import com.taskflow.backend.security.SecurityUtils;
import com.taskflow.backend.task.dto.CreateTaskRequest;
import com.taskflow.backend.task.dto.TaskResponse;
import com.taskflow.backend.task.dto.UpdateTaskRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Business logic for task management and the workflow lifecycle.
 *
 * <p>Every operation is scoped to the authenticated user
 * ({@link SecurityUtils#currentUserId()}); a caller can only see or mutate their
 * own tasks. A supplied {@code categoryId} must reference one of the caller's own
 * categories. {@code completedAt} is managed on transitions to/from {@code DONE}.
 */
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final TaskMapper taskMapper;

    /** Creates a task for the current user. */
    public TaskResponse create(CreateTaskRequest request) {
        String userId = SecurityUtils.currentUserId();
        validateCategory(request.categoryId(), userId);

        TaskStatus status = request.status() != null ? request.status() : TaskStatus.TODO;
        Priority priority = request.priority() != null ? request.priority() : Priority.MEDIUM;

        Task task = Task.builder()
                .userId(userId)
                .categoryId(request.categoryId())
                .title(request.title().trim())
                .description(request.description())
                .status(status)
                .priority(priority)
                .dueDate(request.dueDate())
                .completedAt(status == TaskStatus.DONE ? Instant.now() : null)
                .tags(request.tags())
                .build();

        return taskMapper.toResponse(taskRepository.save(task));
    }

    /**
     * Lists the current user's tasks, filtered/sorted/paginated. The owner scope is
     * always taken from the token — the {@code userId} on the incoming criteria is
     * ignored.
     */
    public PageResponse<TaskResponse> list(TaskSearchCriteria filters, Pageable pageable) {
        String userId = SecurityUtils.currentUserId();
        TaskSearchCriteria scoped = new TaskSearchCriteria(
                userId,
                filters.status(),
                filters.priority(),
                filters.categoryId(),
                filters.dueAfter(),
                filters.dueBefore(),
                filters.overdue(),
                filters.search()
        );
        Page<Task> page = taskRepository.search(scoped, pageable);
        return PageResponse.from(page.map(taskMapper::toResponse));
    }

    /** Returns one of the current user's tasks by id. */
    public TaskResponse get(String id) {
        return taskMapper.toResponse(loadOwned(id, SecurityUtils.currentUserId()));
    }

    /** Updates a task (see {@link UpdateTaskRequest} for field semantics). */
    public TaskResponse update(String id, UpdateTaskRequest request) {
        String userId = SecurityUtils.currentUserId();
        Task task = loadOwned(id, userId);

        task.setTitle(request.title().trim());
        task.setDescription(request.description());
        task.setDueDate(request.dueDate());

        // categoryId: validate-and-replace (null clears the assignment).
        validateCategory(request.categoryId(), userId);
        task.setCategoryId(request.categoryId());

        if (request.priority() != null) {
            task.setPriority(request.priority());
        }
        if (request.tags() != null) {
            task.setTags(request.tags());
        }
        if (request.status() != null) {
            applyStatusTransition(task, request.status());
        }

        return taskMapper.toResponse(taskRepository.save(task));
    }

    /** Deletes one of the current user's tasks. */
    public void delete(String id) {
        Task task = loadOwned(id, SecurityUtils.currentUserId());
        taskRepository.delete(task);
    }

    // ------------------------------------------------------------------

    private Task loadOwned(String id, String userId) {
        return taskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    /** Verifies a (non-null) categoryId belongs to the user; no-op when null/blank. */
    private void validateCategory(String categoryId, String userId) {
        if (categoryId != null && !categoryId.isBlank()
                && categoryRepository.findByIdAndUserId(categoryId, userId).isEmpty()) {
            throw new ResourceNotFoundException("Category not found");
        }
    }

    /** Applies a status change, setting/clearing {@code completedAt} on DONE transitions. */
    private void applyStatusTransition(Task task, TaskStatus newStatus) {
        TaskStatus current = task.getStatus();
        if (newStatus == current) {
            return;
        }
        task.setStatus(newStatus);
        if (newStatus == TaskStatus.DONE) {
            task.setCompletedAt(Instant.now());
        } else if (current == TaskStatus.DONE) {
            task.setCompletedAt(null);
        }
    }
}
