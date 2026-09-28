package com.taskflow.backend.task;

import com.taskflow.backend.common.Priority;
import com.taskflow.backend.common.TaskStatus;
import com.taskflow.backend.ratelimit.RateLimitProperties;
import com.taskflow.backend.ratelimit.RateLimiter;
import com.taskflow.backend.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskCsvExportControllerTest {

    private static final String URL = "/api/v1/tasks/export/csv";

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

    @Test
    void exportReturns200WithCsvHeadersAndBody() throws Exception {
        String csv = """
                Title,Description,Status,Priority,Due Date,Completed At,Tags,Created At,Updated At
                Test Task,A test task,TODO,HIGH,,,,,
                """;

        when(taskCsvExportService.exportTasks(any()))
                .thenReturn(csv.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.parseMediaType("text/csv")))
                .andExpect(header().string(
                        "Content-Disposition",
                        "attachment; filename=\"taskflow-tasks.csv\""))
                .andExpect(content().string(csv));
    }

    @Test
    void exportWithNoTasksReturnsHeaderOnly() throws Exception {
        String header = "Title,Description,Status,Priority,Due Date,Completed At,Tags,Created At,Updated At\n";

        when(taskCsvExportService.exportTasks(any()))
                .thenReturn(header.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(content().string(header));
    }

    @Test
    void exportAcceptsTaskFilters() throws Exception {
        when(taskCsvExportService.exportTasks(any()))
                .thenReturn(
                        "Title,Description,Status,Priority,Due Date,Completed At,Tags,Created At,Updated At\n"
                                .getBytes(java.nio.charset.StandardCharsets.UTF_8));

        mockMvc.perform(get(URL)
                .param("status", "DONE")
                .param("priority", "HIGH")
                .param("categoryId", "category-1")
                .param("search", "project"))
                .andExpect(status().isOk());
    }
}