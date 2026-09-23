/**
 * Request and response DTOs for the task module.
 *
 * <p>DTOs decouple the wire contract from the {@code Task} persistence model and
 * ensure {@code userId} is never accepted from the client. See
 * {@code docs/API_SPEC.md} §4.
 *
 * <ul>
 *   <li>{@code CreateTaskRequest} — title, description, status, priority, dueDate, categoryId, tags.</li>
 *   <li>{@code UpdateTaskRequest} — update of editable fields (title required).</li>
 *   <li>{@code UpdateTaskStatusRequest} — carries only the new status for the dedicated
 *       {@code PATCH /tasks/{id}/status} endpoint.</li>
 *   <li>{@code TaskResponse} — includes the computed {@code isOverdue} flag.</li>
 * </ul>
 *
 * <p>List filters arrive as query parameters bound into
 * {@link com.taskflow.backend.task.TaskSearchCriteria}.
 */
package com.taskflow.backend.task.dto;
