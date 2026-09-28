package com.taskflow.backend.task;

import com.taskflow.backend.common.Priority;
import com.taskflow.backend.common.TaskStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskCsvExportServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskCsvExportService taskCsvExportService;

    @Test
    void exportTasksReturnsHeaderAndTaskData() {
        Task task = Task.builder()
                .id("task-1")
                .userId("user-1")
                .title("Complete project")
                .description("Finish the backend")
                .status(TaskStatus.IN_PROGRESS)
                .priority(Priority.HIGH)
                .tags(List.of("Java", "Spring Boot"))
                .dueDate(Instant.parse("2026-09-30T10:00:00Z"))
                .createdAt(Instant.parse("2026-09-20T10:00:00Z"))
                .updatedAt(Instant.parse("2026-09-25T10:00:00Z"))
                .build();

        when(taskRepository.searchAll(org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of(task));

        try (MockedStatic<com.taskflow.backend.security.SecurityUtils> security = mockStatic(
                com.taskflow.backend.security.SecurityUtils.class)) {

            security.when(com.taskflow.backend.security.SecurityUtils::currentUserId)
                    .thenReturn("user-1");

            byte[] result = taskCsvExportService.exportTasks(
                    new TaskSearchCriteria(
                            null, null, null, null, null, null, null, null));

            String csv = new String(result, java.nio.charset.StandardCharsets.UTF_8);

            assertTrue(csv.startsWith(
                    "Title,Description,Status,Priority,Due Date,Completed At,Tags,Created At,Updated At\n"));

            assertTrue(csv.contains("Complete project"));
            assertTrue(csv.contains("Finish the backend"));
            assertTrue(csv.contains("IN_PROGRESS"));
            assertTrue(csv.contains("HIGH"));
            assertTrue(csv.contains("Java; Spring Boot"));
        }
    }

    @Test
    void exportTasksWithNoTasksReturnsHeaderOnly() {
        when(taskRepository.searchAll(org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of());

        try (MockedStatic<com.taskflow.backend.security.SecurityUtils> security = mockStatic(
                com.taskflow.backend.security.SecurityUtils.class)) {

            security.when(com.taskflow.backend.security.SecurityUtils::currentUserId)
                    .thenReturn("user-1");

            byte[] result = taskCsvExportService.exportTasks(
                    new TaskSearchCriteria(
                            null, null, null, null, null, null, null, null));

            String csv = new String(result, java.nio.charset.StandardCharsets.UTF_8);

            assertEquals(
                    "Title,Description,Status,Priority,Due Date,Completed At,Tags,Created At,Updated At\n",
                    csv);
        }
    }

    @Test
    void exportTasksEscapesCsvSpecialCharacters() {
        Task task = Task.builder()
                .title("Task, with comma")
                .description("Description with \"quotes\" and\nnew line")
                .status(TaskStatus.TODO)
                .priority(Priority.MEDIUM)
                .tags(List.of("Java", "CSV"))
                .build();

        when(taskRepository.searchAll(org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of(task));

        try (MockedStatic<com.taskflow.backend.security.SecurityUtils> security = mockStatic(
                com.taskflow.backend.security.SecurityUtils.class)) {

            security.when(com.taskflow.backend.security.SecurityUtils::currentUserId)
                    .thenReturn("user-1");

            byte[] result = taskCsvExportService.exportTasks(
                    new TaskSearchCriteria(
                            null, null, null, null, null, null, null, null));

            String csv = new String(result, java.nio.charset.StandardCharsets.UTF_8);

            assertTrue(csv.contains("\"Task, with comma\""));
            assertTrue(csv.contains("\"Description with \"\"quotes\"\" and\nnew line\""));
        }
    }
}