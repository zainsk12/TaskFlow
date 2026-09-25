package com.taskflow.backend.task;

import com.taskflow.backend.category.CategoryRepository;
import com.taskflow.backend.common.Priority;
import com.taskflow.backend.common.TaskStatus;
import com.taskflow.backend.task.dto.BulkTaskAction;
import com.taskflow.backend.task.dto.BulkTaskActionRequest;
import com.taskflow.backend.task.dto.BulkTaskActionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TaskService#bulkAction(BulkTaskActionRequest)}.
 *
 * <p>Verifies the happy path, partial-success semantics (invalid IDs collected
 * rather than aborting), authorization enforcement, and missing-parameter
 * validation. No MongoDB instance is required — all repository calls are mocked.
 */
@ExtendWith(MockitoExtension.class)
class BulkTaskActionServiceTest {

    private static final String USER_ID  = "user-42";
    private static final String TASK_ID1 = "task-1";
    private static final String TASK_ID2 = "task-2";
    private static final String TASK_ID3 = "task-3";

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
    // Happy path — all IDs valid
    // ------------------------------------------------------------------

    @Test
    void bulkUpdateStatusChangesAllTasks() {
        Task task1 = buildTask(TASK_ID1, TaskStatus.TODO);
        Task task2 = buildTask(TASK_ID2, TaskStatus.IN_PROGRESS);

        when(taskRepository.findByIdAndUserId(TASK_ID1, USER_ID)).thenReturn(Optional.of(task1));
        when(taskRepository.findByIdAndUserId(TASK_ID2, USER_ID)).thenReturn(Optional.of(task2));
        when(taskRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        BulkTaskActionRequest request = new BulkTaskActionRequest(
                List.of(TASK_ID1, TASK_ID2), BulkTaskAction.UPDATE_STATUS, TaskStatus.DONE);

        BulkTaskActionResponse response = taskService.bulkAction(request);

        assertThat(response.updated()).hasSize(2);
        assertThat(response.updated()).extracting("status")
                .containsOnly(TaskStatus.DONE);
        assertThat(response.notFound()).isEmpty();
    }

    @Test
    void bulkUpdateToDoneSetsCompletedAt() {
        Task task = buildTask(TASK_ID1, TaskStatus.TODO);

        when(taskRepository.findByIdAndUserId(TASK_ID1, USER_ID)).thenReturn(Optional.of(task));
        when(taskRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        Instant before = Instant.now();
        BulkTaskActionRequest request = new BulkTaskActionRequest(
                List.of(TASK_ID1), BulkTaskAction.UPDATE_STATUS, TaskStatus.DONE);

        BulkTaskActionResponse response = taskService.bulkAction(request);

        assertThat(response.updated()).hasSize(1);
        assertThat(response.updated().get(0).completedAt())
                .isNotNull()
                .isAfterOrEqualTo(before);
        assertThat(response.notFound()).isEmpty();
    }

    @Test
    void bulkUpdateFromDoneClearsCompletedAt() {
        Task task = buildTask(TASK_ID1, TaskStatus.DONE);
        task.setCompletedAt(Instant.now().minusSeconds(60));

        when(taskRepository.findByIdAndUserId(TASK_ID1, USER_ID)).thenReturn(Optional.of(task));
        when(taskRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        BulkTaskActionRequest request = new BulkTaskActionRequest(
                List.of(TASK_ID1), BulkTaskAction.UPDATE_STATUS, TaskStatus.IN_PROGRESS);

        BulkTaskActionResponse response = taskService.bulkAction(request);

        assertThat(response.updated().get(0).status()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(response.updated().get(0).completedAt()).isNull();
    }

    @Test
    void unrelatedTaskFieldsAreNotModified() {
        Task task = buildTask(TASK_ID1, TaskStatus.TODO);

        when(taskRepository.findByIdAndUserId(TASK_ID1, USER_ID)).thenReturn(Optional.of(task));
        when(taskRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        BulkTaskActionRequest request = new BulkTaskActionRequest(
                List.of(TASK_ID1), BulkTaskAction.UPDATE_STATUS, TaskStatus.DONE);

        BulkTaskActionResponse response = taskService.bulkAction(request);

        // Only status (and derived completedAt) should change; title and priority must be intact.
        assertThat(response.updated().get(0).title()).isEqualTo("Sample Task");
        assertThat(response.updated().get(0).priority()).isEqualTo(Priority.HIGH);
    }

    // ------------------------------------------------------------------
    // Partial success — mixed valid and invalid IDs
    // ------------------------------------------------------------------

    @Test
    void invalidIdIsCollectedInNotFoundAndOthersAreUpdated() {
        Task task1 = buildTask(TASK_ID1, TaskStatus.TODO);
        Task task3 = buildTask(TASK_ID3, TaskStatus.TODO);

        when(taskRepository.findByIdAndUserId(TASK_ID1, USER_ID)).thenReturn(Optional.of(task1));
        when(taskRepository.findByIdAndUserId(TASK_ID2, USER_ID)).thenReturn(Optional.empty()); // not found
        when(taskRepository.findByIdAndUserId(TASK_ID3, USER_ID)).thenReturn(Optional.of(task3));
        when(taskRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        BulkTaskActionRequest request = new BulkTaskActionRequest(
                List.of(TASK_ID1, TASK_ID2, TASK_ID3), BulkTaskAction.UPDATE_STATUS, TaskStatus.DONE);

        BulkTaskActionResponse response = taskService.bulkAction(request);

        assertThat(response.updated()).hasSize(2);
        assertThat(response.notFound()).containsExactly(TASK_ID2);
    }

    @Test
    void allInvalidIdsProducesEmptyUpdatedList() {
        when(taskRepository.findByIdAndUserId(TASK_ID1, USER_ID)).thenReturn(Optional.empty());
        when(taskRepository.findByIdAndUserId(TASK_ID2, USER_ID)).thenReturn(Optional.empty());
        when(taskRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        BulkTaskActionRequest request = new BulkTaskActionRequest(
                List.of(TASK_ID1, TASK_ID2), BulkTaskAction.UPDATE_STATUS, TaskStatus.DONE);

        BulkTaskActionResponse response = taskService.bulkAction(request);

        assertThat(response.updated()).isEmpty();
        assertThat(response.notFound()).containsExactlyInAnyOrder(TASK_ID1, TASK_ID2);
    }

    // ------------------------------------------------------------------
    // Authorization — another user's task is treated as not-found
    // ------------------------------------------------------------------

    @Test
    void anotherUsersTaskIsReportedAsNotFound() {
        // The repository query is userId-scoped: a different user's task returns empty.
        when(taskRepository.findByIdAndUserId("other-task", USER_ID)).thenReturn(Optional.empty());
        when(taskRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        BulkTaskActionRequest request = new BulkTaskActionRequest(
                List.of("other-task"), BulkTaskAction.UPDATE_STATUS, TaskStatus.DONE);

        BulkTaskActionResponse response = taskService.bulkAction(request);

        assertThat(response.updated()).isEmpty();
        assertThat(response.notFound()).containsExactly("other-task");
    }

    @Test
    void mixedOwnAndOtherUserTasksOnlyUpdatesOwnedTasks() {
        Task ownedTask = buildTask(TASK_ID1, TaskStatus.TODO);

        when(taskRepository.findByIdAndUserId(TASK_ID1, USER_ID)).thenReturn(Optional.of(ownedTask));
        when(taskRepository.findByIdAndUserId("other-task", USER_ID)).thenReturn(Optional.empty());
        when(taskRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        BulkTaskActionRequest request = new BulkTaskActionRequest(
                List.of(TASK_ID1, "other-task"), BulkTaskAction.UPDATE_STATUS, TaskStatus.DONE);

        BulkTaskActionResponse response = taskService.bulkAction(request);

        assertThat(response.updated()).hasSize(1);
        assertThat(response.updated().get(0).id()).isEqualTo(TASK_ID1);
        assertThat(response.notFound()).containsExactly("other-task");
    }

    // ------------------------------------------------------------------
    // Validation — missing action-specific parameters
    // ------------------------------------------------------------------

    @Test
    void updateStatusWithoutStatusFieldReturns400() {
        BulkTaskActionRequest request = new BulkTaskActionRequest(
                List.of(TASK_ID1), BulkTaskAction.UPDATE_STATUS, null /* status missing */);

        assertThatThrownBy(() -> taskService.bulkAction(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("status is required for UPDATE_STATUS");
    }

    // ------------------------------------------------------------------
    // Persistence — saveAll is called even for a single task
    // ------------------------------------------------------------------

    @Test
    void saveAllIsCalledWithTheCorrectTasks() {
        Task task = buildTask(TASK_ID1, TaskStatus.TODO);

        when(taskRepository.findByIdAndUserId(TASK_ID1, USER_ID)).thenReturn(Optional.of(task));
        when(taskRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        BulkTaskActionRequest request = new BulkTaskActionRequest(
                List.of(TASK_ID1), BulkTaskAction.UPDATE_STATUS, TaskStatus.IN_PROGRESS);

        taskService.bulkAction(request);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Task>> captor = ArgumentCaptor.forClass(List.class);
        verify(taskRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private Task buildTask(String id, TaskStatus status) {
        return Task.builder()
                .id(id)
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
