/**
 * Response DTOs for the dashboard module.
 *
 * <p>These are read-only projections of aggregated task data. No DTOs are
 * defined yet.
 *
 * <p>TODO (future phases) — see {@code docs/API_SPEC.md} §6:
 * <ul>
 *   <li>{@code DashboardStatsResponse} — totals (total/todo/inProgress/done/overdue),
 *       byPriority (low/medium/high), completionRate, dueSoon list, generatedAt.</li>
 *   <li>{@code ProductivityResponse} — range plus a daily created/completed series.</li>
 * </ul>
 */
package com.taskflow.backend.dashboard.dto;
