package dev.taskflow.task.dto;

import dev.taskflow.task.Task;
import dev.taskflow.task.TaskPriority;
import dev.taskflow.task.TaskStatus;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
public class TaskResponse {

    private final UUID id;
    private final UUID projectId;
    private final String title;
    private final String description;
    private final TaskStatus status;
    private final TaskPriority priority;
    private final Integer storyPoints;
    private final OffsetDateTime dueDate;
    private final OffsetDateTime createdAt;

    private final AssigneeInfo assignee;
    private final ReporterInfo reporter;
    private final ParentTaskInfo parentTask;

    private final int subtaskCount;

    private TaskResponse(Task task){
        this.id = task.getId();
        this.projectId = task.getProject().getId();
        this.title = task.getTitle();
        this.description = task.getDescription();
        this.status = task.getStatus();
        this.priority = task.getPriority();
        this.storyPoints = task.getStoryPoints();
        this.dueDate = task.getDueDate();
        this.createdAt = task.getCreatedAt();
        this.assignee = task.getAssignee() != null ? new AssigneeInfo(task.getAssignee().getId(), task.getAssignee().getFullName(), task.getAssignee().getEmail()) : null;
        this.reporter = task.getReporter() != null ? new ReporterInfo(task.getReporter().getId(), task.getReporter().getFullName()) : null;
        this.parentTask = task.getParentTask() != null ? new ParentTaskInfo(task.getParentTask().getId(), task.getParentTask().getTitle()) : null;
        this.subtaskCount = task.getSubtasks().size();
    }

    public static TaskResponse from(Task task){
        return new TaskResponse(task);
    }

    public record AssigneeInfo(UUID id, String fullName, String email) {}
    public record ReporterInfo(UUID id, String fullName) {}
    public record ParentTaskInfo(UUID id, String title) {}
}
