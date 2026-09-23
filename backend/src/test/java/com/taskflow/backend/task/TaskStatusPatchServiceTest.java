package com.taskflow.backend.task;

import com.taskflow.backend.category.CategoryRepository;
import com.taskflow.backend.common.Priority;
import com.taskflow.backend.common.ResourceNotFoundException;
import com.taskflow.backend.common.TaskStatus;
import com.taskflow.backend.task.dto.TaskResponse;
import com.taskflow.backend.task.dto.UpdateTaskStatusRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TaskService#updateStatus(String, UpdateTaskStatusRequest)}.
 *
 * <p>Verifies that only the {@code status} (and the derived {@code completedAt})
 * field changes, that authorization is enforced via {@code loadOwned}, and that
 * the correct transitions are applied.
 *
 * <p>No MongoDB instance is required — all repository calls are mocked.
 */
@ExtendWith(MockitoExtension.class)
class TaskStatusPatchServiceTest {

    private static final String USER_ID = "user-42";
    private static final String TASK_ID  = "task-1";

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private CategoryRepository categoryRepository;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository, categoryRepository, new TaskMapper());

        // Stub SecurityContextHolder so SecurityUtils.currentUserId() returns USER_ID.
        var auth = new UsernamePasswordAuthenticationToken(USER_ID, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ------------------------------------------------------------------
    // Happy-path transitions
    // ------------------------------------------------------------------

    @Test
    void statusChangesFromTodoToInProgress() {
        Task task = buildTask(TaskStatus.TODO);
        when(taskRepository.findByIdAndUserId(TASK_ID, USER_ID)).thenReturn(Optional.of(task));
        when(taskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TaskResponse response = taskService.updateStatus(TASK_ID,
                new UpdateTaskStatusRequest(TaskStatus.IN_PROGRESS));

        assertThat(response.status()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(response.completedAt()).isNull();
        // Other fields must be unchanged
        assertThat(response.title()).isEqualTo("Sample Task");
        assertThat(response.priority()).isEqualTo(Priority.HIGH);
    }

    @Test
    void statusChangesToDoneSetsCompletedAt() {
        Task task = buildTask(TaskStatus.IN_PROGRESS);
        when(taskRepository.findByIdAndUserId(TASK_ID, USER_ID)).thenReturn(Optional.of(task));
        when(taskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Instant before = Instant.now();
        TaskResponse response = taskService.updateStatus(TASK_ID,
                new UpdateTaskStatusRequest(TaskStatus.DONE));

        assertThat(response.status()).isEqualTo(TaskStatus.DONE);
        assertThat(response.completedAt()).isNotNull().isAfterOrEqualTo(before);
    }

    @Test
    void statusChangesAwayFromDoneClearsCompletedAt() {
        Task task = buildTask(TaskStatus.DONE);
        task.setCompletedAt(Instant.now().minusSeconds(60));
        when(taskRepository.findByIdAndUserId(TASK_ID, USER_ID)).thenReturn(Optional.of(task));
        when(taskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TaskResponse response = taskService.updateStatus(TASK_ID,
                new UpdateTaskStatusRequest(TaskStatus.IN_PROGRESS));

        assertThat(response.status()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(response.completedAt()).isNull();
    }

    @Test
    void sameStatusIsANoOpButStillSaves() {
        Task task = buildTask(TaskStatus.TODO);
        when(taskRepository.findByIdAndUserId(TASK_ID, USER_ID)).thenReturn(Optional.of(task));
        when(taskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TaskResponse response = taskService.updateStatus(TASK_ID,
                new UpdateTaskStatusRequest(TaskStatus.TODO));

        assertThat(response.status()).isEqualTo(TaskStatus.TODO);

        // save() must still be called (idempotent upsert)
        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(TaskStatus.TODO);
    }

    // ------------------------------------------------------------------
    // Authorization / not-found
    // ------------------------------------------------------------------

    @Test
    void nonExistentTaskThrowsResourceNotFoundException() {
        when(taskRepository.findByIdAndUserId(TASK_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                taskService.updateStatus(TASK_ID,
                        new UpdateTaskStatusRequest(TaskStatus.IN_PROGRESS)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task not found");
    }

    @Test
    void anotherUsersTaskIsNotFoundForCurrentUser() {
        // The repository query is scoped by userId; a different user's task
        // returns empty — indistinguishable from a genuinely missing task.
        when(taskRepository.findByIdAndUserId(TASK_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                taskService.updateStatus(TASK_ID,
                        new UpdateTaskStatusRequest(TaskStatus.DONE)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private Task buildTask(TaskStatus status) {
        return Task.builder()
                .id(TASK_ID)
                .userId(USER_ID)
                .title("Sample Task")
                .description("A description")
                .status(status)
                .priority(Priority.HIGH)
                .tags(List.of("work"))
                .createdAt(Instant.now().minusSeconds(300))
                .updatedAt(Instant.now().minusSeconds(60))
                .build();
    }
}
