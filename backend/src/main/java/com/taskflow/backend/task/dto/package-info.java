/**
 * Request and response DTOs for the task module.
 *
 * <p>DTOs decouple the wire contract from the {@code Task} persistence model and
 * ensure {@code userId} is never accepted from the client. No DTOs are defined yet.
 *
 * <p>TODO (future phases) — see {@code docs/API_SPEC.md} §4:
 * <ul>
 *   <li>{@code TaskResponse} — includes the computed {@code isOverdue} flag.</li>
 *   <li>{@code CreateTaskRequest} — title, description, priority, dueDate, categoryId, tags.</li>
 *   <li>{@code UpdateTaskRequest} — full update of editable fields.</li>
 *   <li>{@code UpdateTaskStatusRequest} — target {@code status} for a workflow transition.</li>
 *   <li>{@code TaskFilter} — query params for status/priority/category/search/overdue.</li>
 * </ul>
 */
package com.taskflow.backend.task.dto;
