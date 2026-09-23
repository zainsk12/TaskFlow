package com.taskflow.backend.task;

import com.taskflow.backend.common.Priority;
import com.taskflow.backend.common.ResourceNotFoundException;
import com.taskflow.backend.common.TaskStatus;
import com.taskflow.backend.ratelimit.RateLimitProperties;
import com.taskflow.backend.ratelimit.RateLimiter;
import com.taskflow.backend.security.JwtService;
import com.taskflow.backend.task.dto.TaskResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests for {@code PATCH /api/v1/tasks/{id}/status}.
 *
 * <p>{@link TaskService} is mocked; only the HTTP contract (routing, request
 * validation, response shape, error mapping) is exercised here. Service-level
 * behaviour is covered by {@link TaskStatusPatchServiceTest}.
 */
@WebMvcTest(controllers = TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskStatusPatchControllerTest {

    private static final String TASK_ID = "task-abc";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    // Required by the filter beans auto-detected during @WebMvcTest slice
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private RateLimiter rateLimiter;
    @MockitoBean
    private RateLimitProperties rateLimitProperties;

    // ------------------------------------------------------------------
    // 200 OK — happy path
    // ------------------------------------------------------------------

    @Test
    void validStatusReturns200WithUpdatedTaskResponse() throws Exception {
        TaskResponse response = buildResponse(TaskStatus.IN_PROGRESS, null);
        when(taskService.updateStatus(eq(TASK_ID), any())).thenReturn(response);

        mockMvc.perform(patch("/api/v1/tasks/{id}/status", TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TASK_ID))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    void transitionToDoneReturns200WithCompletedAt() throws Exception {
        Instant now = Instant.now();
        TaskResponse response = buildResponse(TaskStatus.DONE, now);
        when(taskService.updateStatus(eq(TASK_ID), any())).thenReturn(response);

        mockMvc.perform(patch("/api/v1/tasks/{id}/status", TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DONE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.completedAt").isNotEmpty());
    }

    // ------------------------------------------------------------------
    // 400 — validation failures
    // ------------------------------------------------------------------

    @Test
    void nullStatusFieldReturns400() throws Exception {
        mockMvc.perform(patch("/api/v1/tasks/{id}/status", TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingStatusFieldReturns400() throws Exception {
        mockMvc.perform(patch("/api/v1/tasks/{id}/status", TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownEnumValueReturns400() throws Exception {
        mockMvc.perform(patch("/api/v1/tasks/{id}/status", TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INVALID_STATUS\"}"))
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------------
    // 404 — task not found / not owned
    // ------------------------------------------------------------------

    @Test
    void nonExistentTaskReturns404() throws Exception {
        when(taskService.updateStatus(eq(TASK_ID), any()))
                .thenThrow(new ResourceNotFoundException("Task not found"));

        mockMvc.perform(patch("/api/v1/tasks/{id}/status", TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found"));
    }

    @Test
    void anotherUsersTaskReturns404() throws Exception {
        // Auth enforcement returns the same 404 (not 403) — ownership is opaque.
        when(taskService.updateStatus(eq(TASK_ID), any()))
                .thenThrow(new ResourceNotFoundException("Task not found"));

        mockMvc.perform(patch("/api/v1/tasks/{id}/status", TASK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DONE\"}"))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------------

    private TaskResponse buildResponse(TaskStatus status, Instant completedAt) {
        return new TaskResponse(
                TASK_ID,
                "Test Task",
                "A description",
                status,
                Priority.MEDIUM,
                null,
                completedAt,
                null,
                null,
                false,
                Instant.now().minusSeconds(300),
                Instant.now()
        );
    }
}
