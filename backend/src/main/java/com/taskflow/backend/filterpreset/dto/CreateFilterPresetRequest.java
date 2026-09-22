package com.taskflow.backend.filterpreset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateFilterPresetRequest(

        @NotBlank(message = "name must not be blank")
        @Size(min = 1, max = 40, message = "name must be between 1 and 40 characters")
        String name,

        String status,
        String priority,
        String categoryId,
        String search,
        String sort
) {
}