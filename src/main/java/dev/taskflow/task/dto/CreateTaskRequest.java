package dev.taskflow.task.dto;

import dev.taskflow.task.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class CreateTaskRequest {

    @NotBlank(message = "Task title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String description;

    private TaskPriority priority;

    private Integer storyPoints;

    private OffsetDateTime dueDate;

    private UUID assigneeId;

    private UUID parentTaskId;

}
