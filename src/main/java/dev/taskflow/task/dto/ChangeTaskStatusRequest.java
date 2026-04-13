package dev.taskflow.task.dto;

import dev.taskflow.task.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeTaskStatusRequest {

    @NotNull(message = "Status is required")
    private TaskStatus status;
}
