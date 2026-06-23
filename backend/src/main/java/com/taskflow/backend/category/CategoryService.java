package com.taskflow.backend.category;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Business logic for category management.
 *
 * <p>Enforces per-user ownership and the unique-name-per-user rule. Skeleton
 * only — no logic is implemented in this phase.
 *
 * <p>TODO (future phases):
 * <ul>
 *   <li>CRUD scoped by the authenticated {@code userId}.</li>
 *   <li>Reject duplicate category names for the same user (409 Conflict).</li>
 *   <li>Compute {@code taskCount} per category at read time.</li>
 *   <li>On delete, set {@code categoryId} to null on the user's tasks (tasks are not deleted).</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
}
