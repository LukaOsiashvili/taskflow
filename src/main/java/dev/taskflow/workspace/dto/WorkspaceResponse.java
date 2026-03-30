package dev.taskflow.workspace.dto;

import dev.taskflow.workspace.Workspace;
import dev.taskflow.workspace.WorkspaceMember;
import dev.taskflow.workspace.WorkspaceRole;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
public class WorkspaceResponse {

    private final UUID id;
    private final String name;
    private final String slug;
    private final String description;
    private final UUID ownerId;
    private final String ownerName;
    private final int memberCount;
    private final OffsetDateTime createdAt;

    public static WorkspaceResponse from(Workspace workspace){
        return new WorkspaceResponse(workspace);
    }

    private WorkspaceResponse(Workspace workspace){
        this.id = workspace.getId();
        this.name = workspace.getName();
        this.slug = workspace.getSlug();
        this.description = workspace.getDescription();
        this.ownerId = workspace.getOwner().getId();
        this.ownerName = workspace.getOwner().getFullName();
        this.memberCount = workspace.getMembers().size();
        this.createdAt = workspace.getCreatedAt();
    }

    @Getter
    public static class MemberResponse {
        private final UUID memberId;
        private final UUID userId;
        private final String fullName;
        private final String email;
        private final WorkspaceRole role;
        private final OffsetDateTime joinedAt;

        public static MemberResponse from(WorkspaceMember member){
            return new MemberResponse(member);
        }

        public MemberResponse(WorkspaceMember member){
            this.memberId = member.getId();
            this.userId = member.getUser().getId();
            this.fullName = member.getUser().getFullName();
            this.email = member.getUser().getEmail();
            this.role = member.getRole();
            this.joinedAt = member.getJoinedAt();
        }
    }
}
