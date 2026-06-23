package com.taskflow.backend.task;

import com.taskflow.backend.common.Priority;
import com.taskflow.backend.common.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * Core work item owned by a user — maps to the {@code tasks} collection.
 *
 * <p>Field definitions follow {@code docs/DATABASE.md} §4. Every task is scoped
 * to its owning {@code userId}, which is always set by the backend from the
 * authenticated principal and never accepted from the client.
 *
 * <p>{@code isOverdue} is intentionally <em>not</em> stored — it is computed at
 * read time as {@code dueDate < now && status != DONE}.
 *
 * <p>TODO (future phases):
 * <ul>
 *   <li>Add user-scoped compound indexes (userId+status, userId+dueDate,
 *       userId+priority, userId+categoryId) and a title/description text index.</li>
 *   <li>Default {@code status} to TODO and {@code priority} to MEDIUM in the service layer.</li>
 *   <li>Manage {@code completedAt} on status transitions in the service layer.</li>
 * </ul>
 */
@Document(collection = "tasks")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    private String id;

    /** Owner. References {@code users._id}. Set by the backend, never the client. */
    private String userId;

    /** Optional. References {@code categories._id} owned by the same user. */
    private String categoryId;

    /** Short task title. 1–120 chars. */
    private String title;

    /** Longer details. 0–2000 chars. */
    private String description;

    /** {@link TaskStatus#TODO} / IN_PROGRESS / DONE. Defaults to TODO. */
    private TaskStatus status;

    /** {@link Priority#LOW} / MEDIUM / HIGH. Defaults to MEDIUM. */
    private Priority priority;

    /** Optional deadline. */
    private Instant dueDate;

    /** Set when status becomes DONE; cleared on any transition away from DONE. */
    private Instant completedAt;

    /** Optional free-text tags. */
    private List<String> tags;

    /** Ordering hint within a status column (for a future board view). */
    private Integer position;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
