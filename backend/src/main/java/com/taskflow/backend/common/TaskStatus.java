package com.taskflow.backend.common;

/**
 * Workflow state of a {@code Task} in its lifecycle TODO &rarr; IN_PROGRESS &rarr; DONE.
 *
 * <p>Persisted as a String in the {@code tasks} collection. Defaults to
 * {@link #TODO} on creation. When a task transitions to {@link #DONE} the
 * service layer sets {@code completedAt}; transitioning away from DONE clears it.
 *
 * <p>TODO: reference from the {@code Task} document, the status-transition
 * endpoint, and dashboard aggregation once business logic is implemented.
 */
public enum TaskStatus {

    /** Default status for a newly created task. */
    TODO,

    IN_PROGRESS,

    DONE
}
