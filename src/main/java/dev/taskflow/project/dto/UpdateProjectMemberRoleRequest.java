package dev.taskflow.project.dto;

import dev.taskflow.project.ProjectRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProjectMemberRoleRequest {

    @NotNull(message = "Role is required")

    private ProjectRole role;
}
