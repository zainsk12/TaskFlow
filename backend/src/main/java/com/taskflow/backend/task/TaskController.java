package com.taskflow.backend.task;

import com.taskflow.backend.common.PageResponse;
import com.taskflow.backend.common.Priority;
import com.taskflow.backend.common.TaskStatus;
import com.taskflow.backend.task.dto.BulkTaskActionRequest;
import com.taskflow.backend.task.dto.BulkTaskActionResponse;
import com.taskflow.backend.task.dto.CreateTaskRequest;
import com.taskflow.backend.task.dto.TaskResponse;
import com.taskflow.backend.task.dto.UpdateTaskRequest;
import com.taskflow.backend.task.dto.UpdateTaskStatusRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * REST controller for task resources ({@code /api/v1/tasks}).
 *
 * <p>Thin HTTP adapter: validates request DTOs, maps query parameters into a
 * {@link TaskSearchCriteria}, and delegates to {@link TaskService}. All routes
 * require authentication; the owning user is resolved server-side.
 * See {@code docs/API_SPEC.md} §4.
 */
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /** {@code POST /tasks} — create a task ({@code 201 Created}). */
    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody CreateTaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(request));
    }

    /**
     * {@code GET /tasks} — list the user's tasks with optional filters, plus
     * pagination/sorting (e.g. {@code ?status=TODO&priority=HIGH&sort=dueDate,asc}).
     */
    @GetMapping
    public ResponseEntity<PageResponse<TaskResponse>> list(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) Instant dueAfter,
            @RequestParam(required = false) Instant dueBefore,
            @RequestParam(required = false) Boolean overdue,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        TaskSearchCriteria filters = new TaskSearchCriteria(
                null, status, priority, categoryId, dueAfter, dueBefore, overdue, search);
        return ResponseEntity.ok(taskService.list(filters, pageable));
    }

    /**
     * {@code POST /tasks/bulk-actions} — perform an action on multiple tasks in one
     * request. Returns a {@link BulkTaskActionResponse} that separates successfully
     * updated tasks from IDs that could not be resolved (not found or not owned).
     */
    @PostMapping("/bulk-actions")
    public ResponseEntity<BulkTaskActionResponse> bulkAction(
            @Valid @RequestBody BulkTaskActionRequest request) {

        return ResponseEntity.ok(taskService.bulkAction(request));
    }

    /** {@code GET /tasks/{id}} — get one task. */
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> get(@PathVariable String id) {
        return ResponseEntity.ok(taskService.get(id));
    }

    /** {@code PUT /tasks/{id}} — update a task. */
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> update(@PathVariable String id,
                                               @Valid @RequestBody UpdateTaskRequest request) {
        return ResponseEntity.ok(taskService.update(id, request));
    }

    /** {@code PATCH /tasks/{id}/status} — update only the task status. */
    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponse> updateStatus(@PathVariable String id,
                                                     @Valid @RequestBody UpdateTaskStatusRequest request) {
        return ResponseEntity.ok(taskService.updateStatus(id, request));
    }

    /** {@code DELETE /tasks/{id}} — delete a task ({@code 204 No Content}). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
