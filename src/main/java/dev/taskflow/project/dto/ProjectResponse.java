package dev.taskflow.project.dto;

import dev.taskflow.project.Project;
import dev.taskflow.project.ProjectMember;
import dev.taskflow.project.ProjectRole;
import dev.taskflow.project.ProjectStatus;
import dev.taskflow.workspace.dto.WorkspaceResponse;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
public class ProjectResponse {

    private final UUID id;
    private final UUID workspaceId;
    private final String name;
    private final String description;
    private final ProjectStatus status;
    private final OffsetDateTime startDate;
    private final OffsetDateTime dueDate;
    private final OffsetDateTime createdAt;
    private final List<MemberResponse> members;

    private ProjectResponse(Project project, List<MemberResponse> members){
        this.id = project.getId();
        this.workspaceId = project.getWorkspace().getId();
        this.name = project.getName();
        this.description = project.getDescription();
        this.status = project.getStatus();
        this.startDate = project.getStartDate();
        this.dueDate = project.getDueDate();
        this.createdAt = project.getCreatedAt();
        this.members = members;
    }

    public static ProjectResponse from(Project project){
        List<MemberResponse> members = project.getMembers()
                .stream()
                .map(MemberResponse::from)
                .toList();

        return new ProjectResponse(project, members);
    }

    @Getter
    public static class MemberResponse{

        private final UUID id;
        private final UUID userId;
        private final String fullName;
        private final String email;
        private final ProjectRole role;

        private MemberResponse(ProjectMember member){
            this.id = member.getId();
            this.userId = member.getUser().getId();
            this.fullName = member.getUser().getFullName();
            this.email = member.getUser().getEmail();
            this.role = member.getRole();
        }

        public static MemberResponse from(ProjectMember member){
            return new MemberResponse(member);
        }
    }
}
