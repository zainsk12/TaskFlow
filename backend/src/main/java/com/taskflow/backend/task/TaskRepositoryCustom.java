package com.taskflow.backend.task;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * Custom repository fragment for dynamic search and dashboard aggregations —
 * built with {@code MongoTemplate} where derived queries cannot express the need.
 */
public interface TaskRepositoryCustom {

    /** Returns a page of the user's tasks matching the (optional) criteria. */
    Page<Task> search(TaskSearchCriteria criteria, Pageable pageable);

    /**
     * Counts the user's tasks grouped by status, via a {@code $match} + {@code $group}
     * aggregation. Only non-empty groups are returned (keyed by status name).
     */
    Map<String, Long> countGroupedByStatus(String userId);

    /**
     * Counts the user's tasks grouped by priority, via a {@code $match} + {@code $group}
     * aggregation. Only non-empty groups are returned (keyed by priority name).
     */
    Map<String, Long> countGroupedByPriority(String userId);
}
