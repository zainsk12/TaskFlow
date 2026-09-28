package com.taskflow.backend.task;

import com.taskflow.backend.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskCsvExportService {

    private final TaskRepository taskRepository;

    public byte[] exportTasks(TaskSearchCriteria criteria) {
        String userId = SecurityUtils.currentUserId();

        TaskSearchCriteria userCriteria = new TaskSearchCriteria(
                userId,
                criteria.status(),
                criteria.priority(),
                criteria.categoryId(),
                criteria.dueAfter(),
                criteria.dueBefore(),
                criteria.overdue(),
                criteria.search());

        List<Task> tasks = taskRepository.searchAll(userCriteria);

        StringBuilder csv = new StringBuilder();

        csv.append("Title,Description,Status,Priority,Due Date,Completed At,Tags,Created At,Updated At\n");

        for (Task task : tasks) {
            csv.append(csvValue(task.getTitle())).append(",");
            csv.append(csvValue(task.getDescription())).append(",");
            csv.append(csvValue(task.getStatus())).append(",");
            csv.append(csvValue(task.getPriority())).append(",");
            csv.append(csvValue(task.getDueDate())).append(",");
            csv.append(csvValue(task.getCompletedAt())).append(",");
            csv.append(csvValue(task.getTags() == null
                    ? null
                    : String.join("; ", task.getTags()))).append(",");
            csv.append(csvValue(task.getCreatedAt())).append(",");
            csv.append(csvValue(task.getUpdatedAt())).append("\n");
        }

        return csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private String csvValue(Object value) {
        if (value == null) {
            return "";
        }

        String text = value.toString();

        if (text.contains("\"") || text.contains(",") || text.contains("\n") || text.contains("\r")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }

        return text;
    }
}