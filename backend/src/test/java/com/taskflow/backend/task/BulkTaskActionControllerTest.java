package com.taskflow.backend.task;

import com.taskflow.backend.common.Priority;
import com.taskflow.backend.common.TaskStatus;
import com.taskflow.backend.ratelimit.RateLimitProperties;
import com.taskflow.backend.ratelimit.RateLimiter;
import com.taskflow.backend.security.JwtService;
import com.taskflow.backend.task.dto.BulkTaskActionResponse;
import com.taskflow.backend.task.dto.TaskResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests for {@code POST /api/v1/tasks/bulk-actions}.
 *
 * <p>
 * {@link TaskService} is mocked; only the HTTP contract (routing, request
 * validation, response shape, error mapping) is exercised here. Business-logic
 * behaviour is covered by {@link BulkTaskActionServiceTest}.
 */
@WebMvcTest(controllers = TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class BulkTaskActionControllerTest {

    private static final String URL = "/api/v1/tasks/bulk-actions";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private TaskCsvExportService taskCsvExportService;

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
    void validRequestReturns200WithUpdatedTasks() throws Exception {
        TaskResponse task1 = buildResponse("task-1", TaskStatus.DONE, Instant.now());
        TaskResponse task2 = buildResponse("task-2", TaskStatus.DONE, Instant.now());
        BulkTaskActionResponse serviceResponse = new BulkTaskActionResponse(
                List.of(task1, task2), List.of());

        when(taskService.bulkAction(any())).thenReturn(serviceResponse);

        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "taskIds": ["task-1", "task-2"],
                            "action": "UPDATE_STATUS",
                            "status": "DONE"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updated").isArray())
                .andExpect(jsonPath("$.updated.length()").value(2))
                .andExpect(jsonPath("$.notFound").isArray())
                .andExpect(jsonPath("$.notFound.length()").value(0));
    }

    @Test
    void partialSuccessResponseIncludesNotFoundIds() throws Exception {
        TaskResponse updatedTask = buildResponse("task-1", TaskStatus.DONE, Instant.now());
        BulkTaskActionResponse serviceResponse = new BulkTaskActionResponse(
                List.of(updatedTask), List.of("bad-id"));

        when(taskService.bulkAction(any())).thenReturn(serviceResponse);

        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "taskIds": ["task-1", "bad-id"],
                            "action": "UPDATE_STATUS",
                            "status": "IN_PROGRESS"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updated.length()").value(1))
                .andExpect(jsonPath("$.updated[0].id").value("task-1"))
                .andExpect(jsonPath("$.notFound[0]").value("bad-id"));
    }

    @Test
    void allNotFoundReturns200WithEmptyUpdatedList() throws Exception {
        BulkTaskActionResponse serviceResponse = new BulkTaskActionResponse(
                List.of(), List.of("bad-1", "bad-2"));

        when(taskService.bulkAction(any())).thenReturn(serviceResponse);

        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "taskIds": ["bad-1", "bad-2"],
                            "action": "UPDATE_STATUS",
                            "status": "DONE"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updated.length()").value(0))
                .andExpect(jsonPath("$.notFound.length()").value(2));
    }

    // ------------------------------------------------------------------
    // 400 — request validation failures
    // ------------------------------------------------------------------

    @Test
    void emptyTaskIdsListReturns400() throws Exception {
        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "taskIds": [],
                            "action": "UPDATE_STATUS",
                            "status": "DONE"
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingTaskIdsFieldReturns400() throws Exception {
        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "action": "UPDATE_STATUS",
                            "status": "DONE"
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingActionFieldReturns400() throws Exception {
        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "taskIds": ["task-1"],
                            "status": "DONE"
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownActionEnumValueReturns400() throws Exception {
        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "taskIds": ["task-1"],
                            "action": "INVALID_ACTION",
                            "status": "DONE"
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownStatusEnumValueReturns400() throws Exception {
        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "taskIds": ["task-1"],
                            "action": "UPDATE_STATUS",
                            "status": "NOT_A_VALID_STATUS"
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------------
    // 400 — service-level validation (missing status for UPDATE_STATUS)
    // ------------------------------------------------------------------

    @Test
    void missingStatusForUpdateStatusActionReturns400() throws Exception {
        when(taskService.bulkAction(any()))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST,
                        "status is required for UPDATE_STATUS"));

        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "taskIds": ["task-1"],
                            "action": "UPDATE_STATUS"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("status is required for UPDATE_STATUS"));
    }

    // ------------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------------

    private TaskResponse buildResponse(String id, TaskStatus status, Instant completedAt) {
        return new TaskResponse(
                id,
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
                Instant.now());
    }
}
