package com.taskflow.backend.dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for dashboard statistics ({@code /api/v1/dashboard}).
 *
 * <p>Thin HTTP adapter: delegates to {@link DashboardService} and returns
 * response DTOs. No endpoints are declared yet.
 *
 * <p>TODO (future phases) — see {@code docs/API_SPEC.md} §6:
 * <ul>
 *   <li>{@code GET /dashboard/stats} — totals, by-priority, completion rate, due-soon.</li>
 *   <li>{@code GET /dashboard/productivity} — created/completed series for week/month.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
}
