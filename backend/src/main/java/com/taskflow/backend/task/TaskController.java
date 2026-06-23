package com.taskflow.backend.task;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for task resources ({@code /api/v1/tasks}).
 *
 * <p>Thin HTTP adapter: validates request DTOs, delegates to {@link TaskService},
 * and returns response DTOs with appropriate status codes. No endpoints are
 * declared yet.
 *
 * <p>TODO (future phases) — see {@code docs/API_SPEC.md} §4:
 * <ul>
 *   <li>{@code GET    /tasks} — list with filter/sort/search/paginate.</li>
 *   <li>{@code POST   /tasks} — create.</li>
 *   <li>{@code GET    /tasks/{id}} — get by id.</li>
 *   <li>{@code PUT    /tasks/{id}} — full update.</li>
 *   <li>{@code PATCH  /tasks/{id}/status} — workflow transition.</li>
 *   <li>{@code DELETE /tasks/{id}} — delete.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
}
