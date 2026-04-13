package dev.taskflow.project.dto;

import dev.taskflow.project.ProjectStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeProjectStatusRequest {

    @NotNull(message = "Status is required")

    private ProjectStatus status;
}
