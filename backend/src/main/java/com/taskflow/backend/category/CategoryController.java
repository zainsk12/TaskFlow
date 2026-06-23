package com.taskflow.backend.category;

import com.taskflow.backend.category.dto.CategoryResponse;
import com.taskflow.backend.category.dto.CreateCategoryRequest;
import com.taskflow.backend.category.dto.UpdateCategoryRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for category resources ({@code /api/v1/categories}).
 *
 * <p>Thin HTTP adapter: validates request DTOs and delegates to
 * {@link CategoryService}. All routes require authentication (see
 * {@code SecurityConfig}); the owning user is resolved server-side.
 * See {@code docs/API_SPEC.md} §5.
 */
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /** {@code POST /categories} — create a category ({@code 201 Created}). */
    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(request));
    }

    /** {@code GET /categories} — list the user's categories. */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> list() {
        return ResponseEntity.ok(categoryService.list());
    }

    /** {@code GET /categories/{id}} — get one category. */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> get(@PathVariable String id) {
        return ResponseEntity.ok(categoryService.get(id));
    }

    /** {@code PUT /categories/{id}} — update a category. */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(@PathVariable String id,
                                                   @Valid @RequestBody UpdateCategoryRequest request) {
        return ResponseEntity.ok(categoryService.update(id, request));
    }

    /** {@code DELETE /categories/{id}} — delete a category ({@code 204 No Content}). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
