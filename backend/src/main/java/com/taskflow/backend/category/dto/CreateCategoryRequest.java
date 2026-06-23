package com.taskflow.backend.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request body for {@code POST /api/v1/categories} — see {@code docs/API_SPEC.md} §5.2.
 *
 * @param name  category label (required, 1–40 chars, unique per user)
 * @param color 6-digit hex colour, e.g. {@code #54C8EE}
 */
public record CreateCategoryRequest(

        @NotBlank(message = "name must not be blank")
        @Size(min = 1, max = 40, message = "name must be between 1 and 40 characters")
        String name,

        @NotBlank(message = "color must not be blank")
        @Pattern(regexp = "^#([0-9A-Fa-f]{6})$", message = "color must be a 6-digit hex code, e.g. #54C8EE")
        String color
) {
}
