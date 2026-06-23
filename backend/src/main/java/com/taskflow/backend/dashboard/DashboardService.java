package com.taskflow.backend.dashboard;

import com.taskflow.backend.category.CategoryRepository;
import com.taskflow.backend.common.Priority;
import com.taskflow.backend.common.TaskStatus;
import com.taskflow.backend.dashboard.dto.DashboardSummaryResponse;
import com.taskflow.backend.dashboard.dto.ProductivityResponse;
import com.taskflow.backend.security.SecurityUtils;
import com.taskflow.backend.task.Task;
import com.taskflow.backend.task.TaskMapper;
import com.taskflow.backend.task.TaskRepository;
import com.taskflow.backend.task.TaskSearchCriteria;
import com.taskflow.backend.task.dto.TaskResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Read-only aggregation logic for the productivity dashboard.
 *
 * <p>Every metric is scoped to the authenticated user
 * ({@link SecurityUtils#currentUserId()}) — a caller only ever sees their own
 * data. Status/priority breakdowns use MongoDB {@code $match}+{@code $group}
 * aggregations; the headline counts use indexed count queries. Nothing here
 * mutates state. See {@code docs/API_SPEC.md} §6.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    /** Default number of recent tasks returned, and the hard cap. */
    private static final int DEFAULT_RECENT_LIMIT = 5;
    private static final int MAX_RECENT_LIMIT = 50;

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final TaskMapper taskMapper;

    /** Headline snapshot — totals, overdue, completion rate, high-priority, categories. */
    public DashboardSummaryResponse getSummary() {
        String userId = SecurityUtils.currentUserId();
        Map<String, Long> byStatus = taskRepository.countGroupedByStatus(userId);

        long total = taskRepository.countByUserId(userId);
        long todo = byStatus.getOrDefault(TaskStatus.TODO.name(), 0L);
        long inProgress = byStatus.getOrDefault(TaskStatus.IN_PROGRESS.name(), 0L);
        long done = byStatus.getOrDefault(TaskStatus.DONE.name(), 0L);
        long overdue = taskRepository.countByUserIdAndStatusNotAndDueDateBefore(
                userId, TaskStatus.DONE, Instant.now());
        long high = taskRepository.countByUserIdAndPriority(userId, Priority.HIGH);
        long categories = categoryRepository.countByUserId(userId);

        return new DashboardSummaryResponse(
                total, todo, inProgress, done, overdue,
                percentage(done, total), high, categories);
    }

    /** Task counts by status, zero-filled for every {@link TaskStatus}. */
    public Map<String, Long> getStatusDistribution() {
        String userId = SecurityUtils.currentUserId();
        Map<String, Long> grouped = taskRepository.countGroupedByStatus(userId);
        Map<String, Long> result = new LinkedHashMap<>();
        for (TaskStatus status : TaskStatus.values()) {
            result.put(status.name(), grouped.getOrDefault(status.name(), 0L));
        }
        return result;
    }

    /** Task counts by priority, zero-filled for every {@link Priority}. */
    public Map<String, Long> getPriorityDistribution() {
        String userId = SecurityUtils.currentUserId();
        Map<String, Long> grouped = taskRepository.countGroupedByPriority(userId);
        Map<String, Long> result = new LinkedHashMap<>();
        for (Priority priority : Priority.values()) {
            result.put(priority.name(), grouped.getOrDefault(priority.name(), 0L));
        }
        return result;
    }

    /** The user's most recently created tasks (newest first), capped at {@value #MAX_RECENT_LIMIT}. */
    public List<TaskResponse> getRecentTasks(Integer limit) {
        String userId = SecurityUtils.currentUserId();
        int size = clampLimit(limit);
        TaskSearchCriteria criteria = new TaskSearchCriteria(userId, null, null, null, null, null, null, null);
        PageRequest pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return taskRepository.search(criteria, pageable).getContent().stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    /** Derived productivity metrics: completion %, overdue %, and active (open) workload. */
    public ProductivityResponse getProductivity() {
        String userId = SecurityUtils.currentUserId();
        Map<String, Long> byStatus = taskRepository.countGroupedByStatus(userId);

        long total = taskRepository.countByUserId(userId);
        long done = byStatus.getOrDefault(TaskStatus.DONE.name(), 0L);
        long todo = byStatus.getOrDefault(TaskStatus.TODO.name(), 0L);
        long inProgress = byStatus.getOrDefault(TaskStatus.IN_PROGRESS.name(), 0L);
        long overdue = taskRepository.countByUserIdAndStatusNotAndDueDateBefore(
                userId, TaskStatus.DONE, Instant.now());

        return new ProductivityResponse(
                percentage(done, total),
                percentage(overdue, total),
                todo + inProgress);
    }

    // ------------------------------------------------------------------

    /** {@code part / whole * 100} rounded to 1 decimal; {@code 0} when {@code whole == 0}. */
    private double percentage(long part, long whole) {
        if (whole <= 0) {
            return 0.0;
        }
        return Math.round((part * 1000.0 / whole)) / 10.0;
    }

    private int clampLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_RECENT_LIMIT;
        }
        return Math.min(limit, MAX_RECENT_LIMIT);
    }
}
