package com.taskflow.backend.dashboard;

import com.taskflow.backend.task.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Read-only aggregation logic for the productivity dashboard.
 *
 * <p>Computes statistics over the authenticated user's tasks using MongoDB
 * aggregation. Skeleton only — no logic is implemented in this phase.
 *
 * <p>TODO (future phases) — see {@code docs/API_SPEC.md} §6:
 * <ul>
 *   <li>{@code getStats(userId)} — counts by status, overdue count, counts by
 *       priority, completion rate (done/total), and up to 5 due-soon tasks.</li>
 *   <li>{@code getProductivity(userId, range)} — created/completed series over week/month.</li>
 *   <li>Scope every aggregation by {@code userId} ({@code $match} then {@code $group}).</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TaskRepository taskRepository;
}
