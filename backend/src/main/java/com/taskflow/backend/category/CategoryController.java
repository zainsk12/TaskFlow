package com.taskflow.backend.category;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for category resources ({@code /api/v1/categories}).
 *
 * <p>Thin HTTP adapter: validates request DTOs, delegates to
 * {@link CategoryService}, and returns response DTOs. No endpoints are declared yet.
 *
 * <p>TODO (future phases) — see {@code docs/API_SPEC.md} §5:
 * <ul>
 *   <li>{@code GET    /categories} — list (with computed taskCount).</li>
 *   <li>{@code POST   /categories} — create.</li>
 *   <li>{@code GET    /categories/{id}} — get by id.</li>
 *   <li>{@code PUT    /categories/{id}} — update.</li>
 *   <li>{@code DELETE /categories/{id}} — delete (nulls tasks' categoryId).</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
}
