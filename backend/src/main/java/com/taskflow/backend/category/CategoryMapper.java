package com.taskflow.backend.category;

import com.taskflow.backend.category.dto.CategoryResponse;
import org.springframework.stereotype.Component;

/**
 * Explicit mapper between the {@link Category} document and its wire DTO.
 *
 * <p>{@code taskCount} is not stored on the document, so it is supplied by the
 * service (computed at read time) when mapping.
 */
@Component
public class CategoryMapper {

    /**
     * Maps a category to its response view.
     *
     * @param category  the document
     * @param taskCount number of the user's tasks in this category
     */
    public CategoryResponse toResponse(Category category, long taskCount) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getColor(),
                taskCount,
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
