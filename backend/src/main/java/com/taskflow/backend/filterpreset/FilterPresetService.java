package com.taskflow.backend.filterpreset;

import com.taskflow.backend.common.DuplicateResourceException;
import com.taskflow.backend.common.ResourceNotFoundException;
import com.taskflow.backend.filterpreset.dto.CreateFilterPresetRequest;
import com.taskflow.backend.filterpreset.dto.FilterPresetResponse;
import com.taskflow.backend.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FilterPresetService {

    private final FilterPresetRepository filterPresetRepository;

    public FilterPresetResponse create(CreateFilterPresetRequest request) {
        String userId = SecurityUtils.currentUserId();
        String name = request.name().trim();

        if (filterPresetRepository.existsByUserIdAndName(userId, name)) {
            throw new DuplicateResourceException(
                    "A filter preset named '" + name + "' already exists"
            );
        }

        FilterPreset preset = FilterPreset.builder()
                .userId(userId)
                .name(name)
                .status(normalize(request.status()))
                .priority(normalize(request.priority()))
                .categoryId(normalize(request.categoryId()))
                .search(normalize(request.search()))
                .sort(normalizeSort(request.sort()))
                .build();

        return toResponse(filterPresetRepository.save(preset));
    }

    public List<FilterPresetResponse> list() {
        String userId = SecurityUtils.currentUserId();

        return filterPresetRepository
                .findByUserId(userId, Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void delete(String id) {
        String userId = SecurityUtils.currentUserId();

        FilterPreset preset = filterPresetRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Filter preset not found"));

        filterPresetRepository.delete(preset);
    }

    private FilterPresetResponse toResponse(FilterPreset preset) {
        return new FilterPresetResponse(
                preset.getId(),
                preset.getName(),
                preset.getStatus(),
                preset.getPriority(),
                preset.getCategoryId(),
                preset.getSearch(),
                preset.getSort(),
                preset.getCreatedAt(),
                preset.getUpdatedAt()
        );
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim();
    }

    private String normalizeSort(String value) {
        String normalized = normalize(value);
        return normalized.isEmpty() ? "createdAt,desc" : normalized;
    }
}