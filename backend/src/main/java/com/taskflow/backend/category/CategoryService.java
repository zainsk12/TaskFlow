package com.taskflow.backend.category;

import com.taskflow.backend.category.dto.CategoryResponse;
import com.taskflow.backend.category.dto.CreateCategoryRequest;
import com.taskflow.backend.category.dto.UpdateCategoryRequest;
import com.taskflow.backend.common.DuplicateResourceException;
import com.taskflow.backend.common.ResourceNotFoundException;
import com.taskflow.backend.security.SecurityUtils;
import com.taskflow.backend.task.Task;
import com.taskflow.backend.task.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Business logic for category management.
 *
 * <p>Every operation is scoped to the authenticated user (via
 * {@link SecurityUtils#currentUserId()}), so a caller can only ever see or mutate
 * their own categories. Enforces the unique-name-per-user rule ({@code 409}) and
 * computes {@code taskCount} at read time.
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TaskRepository taskRepository;
    private final CategoryMapper categoryMapper;

    /** Creates a category for the current user. */
    public CategoryResponse create(CreateCategoryRequest request) {
        String userId = SecurityUtils.currentUserId();
        String name = request.name().trim();

        if (categoryRepository.existsByUserIdAndName(userId, name)) {
            throw new DuplicateResourceException("A category named '" + name + "' already exists");
        }

        Category category = Category.builder()
                .userId(userId)
                .name(name)
                .color(request.color())
                .build();

        Category saved = categoryRepository.save(category);
        // A brand-new category has no tasks yet.
        return categoryMapper.toResponse(saved, 0);
    }

    /** Lists the current user's categories (ordered by name), each with its task count. */
    public List<CategoryResponse> list() {
        String userId = SecurityUtils.currentUserId();
        return categoryRepository.findByUserId(userId, Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(c -> categoryMapper.toResponse(c, taskRepository.countByUserIdAndCategoryId(userId, c.getId())))
                .toList();
    }

    /** Returns one of the current user's categories by id. */
    public CategoryResponse get(String id) {
        String userId = SecurityUtils.currentUserId();
        Category category = loadOwned(id, userId);
        return categoryMapper.toResponse(category, taskRepository.countByUserIdAndCategoryId(userId, id));
    }

    /** Updates a category's name/color (full update). */
    public CategoryResponse update(String id, UpdateCategoryRequest request) {
        String userId = SecurityUtils.currentUserId();
        Category category = loadOwned(id, userId);
        String name = request.name().trim();

        if (categoryRepository.existsByUserIdAndNameAndIdNot(userId, name, id)) {
            throw new DuplicateResourceException("A category named '" + name + "' already exists");
        }

        category.setName(name);
        category.setColor(request.color());
        Category saved = categoryRepository.save(category);
        return categoryMapper.toResponse(saved, taskRepository.countByUserIdAndCategoryId(userId, id));
    }

    /**
     * Deletes a category. Its tasks are <strong>not</strong> deleted — their
     * {@code categoryId} is cleared so work is never lost (see {@code DATABASE.md} §8).
     */
    public void delete(String id) {
        String userId = SecurityUtils.currentUserId();
        Category category = loadOwned(id, userId);

        List<Task> tasks = taskRepository.findByUserIdAndCategoryId(userId, id);
        if (!tasks.isEmpty()) {
            tasks.forEach(t -> t.setCategoryId(null));
            taskRepository.saveAll(tasks);
        }

        categoryRepository.delete(category);
    }

    private Category loadOwned(String id, String userId) {
        return categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }
}
