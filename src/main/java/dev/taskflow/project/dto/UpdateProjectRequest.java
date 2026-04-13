package dev.taskflow.project.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class UpdateProjectRequest {


    @Size(max = 255, message = "Project name must not exceed 255 characters")
    private String name;

    private String description;

    private OffsetDateTime startDate;
    private OffsetDateTime dueDate;
}
