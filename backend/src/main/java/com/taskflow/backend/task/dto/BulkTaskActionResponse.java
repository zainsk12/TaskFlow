package com.taskflow.backend.task.dto;

import java.util.List;

/**
 * Response body for {@code POST /api/v1/tasks/bulk-actions}.
 *
 * <p>Summarises the outcome of a bulk operation. Tasks that were successfully
 * acted on are listed in {@code updated}. IDs that could not be resolved (not
 * found or not owned by the caller) are collected in {@code notFound} so the
 * caller can tell exactly which IDs were skipped without the whole request
 * being rejected.
 *
 * @param updated  task views for every successfully updated task (may be empty)
 * @param notFound task IDs that were not found or not owned by the caller (may be empty)
 */
public record BulkTaskActionResponse(
        List<TaskResponse> updated,
        List<String> notFound
) {
}
