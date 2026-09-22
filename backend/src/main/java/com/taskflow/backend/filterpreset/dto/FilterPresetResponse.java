package com.taskflow.backend.filterpreset.dto;

import java.time.Instant;

public record FilterPresetResponse(
        String id,
        String name,
        String status,
        String priority,
        String categoryId,
        String search,
        String sort,
        Instant createdAt,
        Instant updatedAt
) {
}