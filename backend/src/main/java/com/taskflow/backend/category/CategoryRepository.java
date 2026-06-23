package com.taskflow.backend.category;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for {@link Category} documents.
 *
 * <p>Provides the standard CRUD contract over the {@code categories} collection.
 * No custom queries are defined yet.
 *
 * <p>TODO (future phases): add user-scoped queries — e.g. find all by
 * {@code userId}, find by id and {@code userId}, existence check by
 * {@code (userId, name)} for duplicate detection, and delete-by-userId for the
 * account-deletion cascade.
 */
@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {
}
