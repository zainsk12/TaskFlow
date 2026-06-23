package com.taskflow.backend.dashboard.dto;

/**
 * Response body for {@code GET /api/v1/dashboard/productivity}.
 *
 * <p>Derived productivity metrics for the authenticated user.
 *
 * @param completionPercentage {@code completedTasks / totalTasks * 100}, 1 decimal
 * @param overduePercentage    {@code overdueTasks / totalTasks * 100}, 1 decimal
 * @param activeWorkload       open tasks not yet done ({@code TODO + IN_PROGRESS})
 */
public record ProductivityResponse(
        double completionPercentage,
        double overduePercentage,
        long activeWorkload
) {
}
