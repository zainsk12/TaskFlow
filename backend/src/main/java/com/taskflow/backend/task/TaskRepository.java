package com.taskflow.backend.task;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository for {@link Task} documents.
 *
 * <p>Standard CRUD plus user-scoped lookups; dynamic filtered/paged search lives
 * in {@link TaskRepositoryCustom} (implemented by {@code TaskRepositoryImpl}).
 * Every query is scoped by {@code userId} so a caller only touches their own tasks.
 */
@Repository
public interface TaskRepository extends MongoRepository<Task, String>, TaskRepositoryCustom {

    /** A single task by id, but only if owned by the given user. */
    Optional<Task> findByIdAndUserId(String id, String userId);

    /** Number of the user's tasks assigned to a category (for {@code taskCount}). */
    long countByUserIdAndCategoryId(String userId, String categoryId);

    /** The user's tasks in a category (used to clear {@code categoryId} on category delete). */
    List<Task> findByUserIdAndCategoryId(String userId, String categoryId);
}
