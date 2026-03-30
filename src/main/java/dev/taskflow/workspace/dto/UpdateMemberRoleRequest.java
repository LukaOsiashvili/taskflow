package dev.taskflow.workspace.dto;

import dev.taskflow.workspace.WorkspaceRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMemberRoleRequest {

    @NotNull(message = "Role is required")

    private WorkspaceRole role;

}
