package com.taskflow.backend.task;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Custom repository fragment for dynamic, paginated task search — the optional
 * combination of filters in {@link TaskSearchCriteria} cannot be expressed as a
 * single derived query, so it is built with {@code MongoTemplate}.
 */
public interface TaskRepositoryCustom {

    /** Returns a page of the user's tasks matching the (optional) criteria. */
    Page<Task> search(TaskSearchCriteria criteria, Pageable pageable);
}
