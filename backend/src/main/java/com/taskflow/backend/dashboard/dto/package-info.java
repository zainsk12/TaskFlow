/**
 * Response DTOs for the dashboard module.
 *
 * <p>Read-only projections of aggregated task data (see {@code docs/API_SPEC.md} §6):
 * <ul>
 *   <li>{@code DashboardSummaryResponse} — totals by status, overdue, completion
 *       rate, high-priority count, categories count.</li>
 *   <li>{@code ProductivityResponse} — completion %, overdue %, active workload.</li>
 * </ul>
 *
 * <p>The status- and priority-distribution endpoints return a
 * {@code Map<String, Long>} keyed by the enum names ({@code TODO}/{@code IN_PROGRESS}/
 * {@code DONE}, {@code LOW}/{@code MEDIUM}/{@code HIGH}); recent tasks reuse
 * {@link com.taskflow.backend.task.dto.TaskResponse}.
 */
package com.taskflow.backend.dashboard.dto;
