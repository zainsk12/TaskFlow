package com.taskflow.backend.category;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository for {@link Category} documents.
 *
 * <p>All queries are scoped by {@code userId} so a caller can only ever touch
 * their own categories.
 */
@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {

    /** All of a user's categories, ordered. */
    List<Category> findByUserId(String userId, Sort sort);

    /** A single category by id, but only if owned by the given user. */
    Optional<Category> findByIdAndUserId(String id, String userId);

    /** Whether the user already has a category with this name. */
    boolean existsByUserIdAndName(String userId, String name);

    /** Whether the user has a <em>different</em> category with this name (rename guard). */
    boolean existsByUserIdAndNameAndIdNot(String userId, String name, String id);

    /** Total number of the user's categories (dashboard). */
    long countByUserId(String userId);
}
