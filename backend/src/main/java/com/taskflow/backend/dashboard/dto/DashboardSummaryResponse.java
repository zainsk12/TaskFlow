package com.taskflow.backend.dashboard.dto;

/**
 * Response body for {@code GET /api/v1/dashboard/summary}.
 *
 * <p>A headline snapshot of the authenticated user's tasks: totals by status,
 * overdue count, completion rate, high-priority count, and the number of
 * categories they own.
 *
 * @param totalTasks        all of the user's tasks
 * @param todoTasks         tasks in {@code TODO}
 * @param inProgressTasks   tasks in {@code IN_PROGRESS}
 * @param completedTasks    tasks in {@code DONE}
 * @param overdueTasks      tasks past due and not {@code DONE}
 * @param completionRate    {@code completedTasks / totalTasks * 100}, 1 decimal (0 when no tasks)
 * @param highPriorityTasks tasks with priority {@code HIGH}
 * @param categoriesCount   number of categories the user owns
 */
public record DashboardSummaryResponse(
        long totalTasks,
        long todoTasks,
        long inProgressTasks,
        long completedTasks,
        long overdueTasks,
        double completionRate,
        long highPriorityTasks,
        long categoriesCount
) {
}
