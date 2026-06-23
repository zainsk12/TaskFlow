package com.taskflow.backend.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * User-defined, colour-coded grouping of tasks — maps to the {@code categories}
 * collection.
 *
 * <p>Field definitions follow {@code docs/DATABASE.md} §3. Each category is
 * scoped to its owning {@code userId}; the pair {@code (userId, name)} is unique.
 *
 * <p>{@code taskCount} (exposed in the API) is intentionally <em>not</em> stored
 * — it is computed at read time.
 *
 * <p>The unique compound index {@code (userId, name)} enforces one category name
 * per user at the database level (defence in depth alongside the service check).
 */
@Document(collection = "categories")
@CompoundIndex(name = "uniq_user_name", def = "{'userId': 1, 'name': 1}", unique = true)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    private String id;

    /** Owner. References {@code users._id}. Set by the backend, never the client. */
    private String userId;

    /** Category label. 1–40 chars. Unique per user. */
    private String name;

    /** Hex colour used in the UI, e.g. {@code #54C8EE}. */
    private String color;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
