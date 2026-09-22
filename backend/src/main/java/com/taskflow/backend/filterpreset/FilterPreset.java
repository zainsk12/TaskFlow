package com.taskflow.backend.filterpreset;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "filter_presets")
@CompoundIndex(
        name = "uniq_user_name",
        def = "{'userId': 1, 'name': 1}",
        unique = true
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterPreset {

    @Id
    private String id;

    private String userId;
    private String name;

    private String status;
    private String priority;
    private String categoryId;
    private String search;
    private String sort;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}