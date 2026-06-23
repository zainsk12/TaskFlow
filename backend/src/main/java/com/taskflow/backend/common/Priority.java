package com.taskflow.backend.common;

/**
 * Importance level of a {@code Task}.
 *
 * <p>Used for filtering, sorting, and dashboard breakdowns. Persisted as a
 * String in the {@code tasks} collection. Defaults to {@link #MEDIUM} when a
 * task is created without an explicit priority.
 *
 * <p>TODO: reference from the {@code Task} document, request/response DTOs, and
 * dashboard aggregation once business logic is implemented.
 */
public enum Priority {

    LOW,

    /** Default priority applied when none is supplied on task creation. */
    MEDIUM,

    HIGH
}
