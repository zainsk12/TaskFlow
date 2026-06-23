package com.taskflow.backend.task;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for {@link Task} documents.
 *
 * <p>Provides the standard CRUD contract over the {@code tasks} collection. No
 * custom queries are defined yet.
 *
 * <p>TODO (future phases): add user-scoped, paged, and filtered queries —
 * e.g. find by {@code userId} with optional status/priority/category filters and
 * keyword search; count-by-status/priority for the dashboard; delete-by-userId
 * for the account-deletion cascade.
 */
@Repository
public interface TaskRepository extends MongoRepository<Task, String> {
}
