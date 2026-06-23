package com.taskflow.backend.dashboard;

import com.taskflow.backend.dashboard.dto.DashboardSummaryResponse;
import com.taskflow.backend.dashboard.dto.ProductivityResponse;
import com.taskflow.backend.task.dto.TaskResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST controller for dashboard analytics ({@code /api/v1/dashboard}).
 *
 * <p>Thin, read-only HTTP adapter that delegates to {@link DashboardService}. All
 * routes require authentication; every metric is scoped to the caller server-side.
 * See {@code docs/API_SPEC.md} §6.
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /** {@code GET /dashboard/summary} — headline counts and completion rate. */
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> summary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    /** {@code GET /dashboard/status-distribution} — task counts by status. */
    @GetMapping("/status-distribution")
    public ResponseEntity<Map<String, Long>> statusDistribution() {
        return ResponseEntity.ok(dashboardService.getStatusDistribution());
    }

    /** {@code GET /dashboard/priority-distribution} — task counts by priority. */
    @GetMapping("/priority-distribution")
    public ResponseEntity<Map<String, Long>> priorityDistribution() {
        return ResponseEntity.ok(dashboardService.getPriorityDistribution());
    }

    /** {@code GET /dashboard/recent-tasks?limit=5} — the user's newest tasks. */
    @GetMapping("/recent-tasks")
    public ResponseEntity<List<TaskResponse>> recentTasks(@RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(dashboardService.getRecentTasks(limit));
    }

    /** {@code GET /dashboard/productivity} — completion %, overdue %, active workload. */
    @GetMapping("/productivity")
    public ResponseEntity<ProductivityResponse> productivity() {
        return ResponseEntity.ok(dashboardService.getProductivity());
    }
}
