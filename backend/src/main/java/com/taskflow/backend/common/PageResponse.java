package com.taskflow.backend.common;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Wire wrapper for paginated list responses — the shape documented in
 * {@code docs/API_SPEC.md} §1.4.
 *
 * @param content       the page's items (already mapped to response DTOs)
 * @param page          zero-based page index
 * @param size          page size
 * @param totalElements total number of matching elements across all pages
 * @param totalPages    total number of pages
 * @param hasNext       whether a further page exists
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {

    /**
     * Builds a {@link PageResponse} from a Spring Data {@link Page} whose content
     * has already been mapped to the DTO type {@code T}.
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}
