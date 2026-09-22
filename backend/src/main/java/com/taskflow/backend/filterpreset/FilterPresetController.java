package com.taskflow.backend.filterpreset;

import com.taskflow.backend.filterpreset.dto.CreateFilterPresetRequest;
import com.taskflow.backend.filterpreset.dto.FilterPresetResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/filter-presets")
@RequiredArgsConstructor
public class FilterPresetController {

    private final FilterPresetService filterPresetService;

    @PostMapping
    public ResponseEntity<FilterPresetResponse> create(
            @Valid @RequestBody CreateFilterPresetRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(filterPresetService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<FilterPresetResponse>> list() {
        return ResponseEntity.ok(filterPresetService.list());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        filterPresetService.delete(id);
        return ResponseEntity.noContent().build();
    }
}