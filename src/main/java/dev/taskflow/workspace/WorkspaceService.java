package dev.taskflow.workspace;

import dev.taskflow.common.exception.AccessDeniedException;
import dev.taskflow.common.exception.BusinessRuleException;
import dev.taskflow.common.exception.ResourceNotFoundException;
import dev.taskflow.common.util.SlugUtils;
import dev.taskflow.user.User;
import dev.taskflow.user.UserRepository;
import dev.taskflow.user.UserService;
import dev.taskflow.workspace.dto.*;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Transactional
    public WorkspaceResponse create(CreateWorkspaceRequest request) {
        User currentUser = userService.resolveCurrentUser();

        String slug = SlugUtils.toUniqueSlug(request.getName(), workspaceRepository::existsBySlug);

        Workspace workspace = Workspace.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .owner(currentUser)
                .build();

        workspaceRepository.save(workspace);

        WorkspaceMember ownerMember = WorkspaceMember.builder()
                .workspace(workspace)
                .user(currentUser)
                .role(WorkspaceRole.OWNER)
                .build();

        workspaceMemberRepository.save(ownerMember);
        workspace.getMembers().add(ownerMember);

        workspaceRepository.flush();

        return WorkspaceResponse.from(workspace);
    }

    @Transactional(readOnly = true)
    public WorkspaceResponse getById(UUID workspaceId) {
        Workspace workspace = findWorkspaceOrThrow(workspaceId);
        requireMember(workspace, userService.resolveCurrentUser());
        return WorkspaceResponse.from(workspace);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getMyWorkspaces() {
        User currentUser = userService.resolveCurrentUser();

        return workspaceMemberRepository.findAllByUserId(currentUser.getId())
                .stream()
                .map(wm -> WorkspaceResponse.from(wm.getWorkspace()))
                .toList();
    }

    @Transactional
    public WorkspaceResponse update(UUID workspaceId, UpdateWorkspaceRequest request) {
        User currentUser = userService.resolveCurrentUser();
        Workspace workspace = findWorkspaceOrThrow(workspaceId);
        requireRole(workspace, currentUser, WorkspaceRole.ADMIN);

        workspace.setName(request.getName());
        workspace.setDescription(request.getDescription());

        return WorkspaceResponse.from(workspace);
    }

    @Transactional
    public void delete(UUID workspaceId) {
        User currentUser = userService.resolveCurrentUser();
        Workspace workspace = findWorkspaceOrThrow(workspaceId);
        requireRole(workspace, currentUser, WorkspaceRole.OWNER);

        workspaceRepository.delete(workspace);
    }

    @Transactional
    public WorkspaceResponse.MemberResponse inviteMember(UUID workspaceId, InviteMemberRequest request) {
        User currentUser = userService.resolveCurrentUser();
        Workspace workspace = findWorkspaceOrThrow(workspaceId);
        requireRole(workspace, currentUser, WorkspaceRole.ADMIN);

        if (request.getRole() == WorkspaceRole.OWNER) {
            throw new BusinessRuleException("Cannot assign OWNER role via invite. Use ownership transfer");
        }

        User invitee = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new ResourceNotFoundException("No user found with email: " + request.getEmail()));

        if (workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, invitee.getId())) {
            throw new BusinessRuleException("User is already a member of this workspace");
        }

        WorkspaceMember member = WorkspaceMember.builder()
                .workspace(workspace)
                .user(invitee)
                .role(request.getRole())
                .build();

        workspaceMemberRepository.save(member);
        workspaceMemberRepository.flush();

        return WorkspaceResponse.MemberResponse.from(member);
    }

    @Transactional
    public WorkspaceResponse.MemberResponse updateMemberRole(UUID workspaceId, UUID userId, UpdateMemberRoleRequest request) {
        User currentUser = userService.resolveCurrentUser();
        Workspace workspace = findWorkspaceOrThrow(workspaceId);
        requireRole(workspace, currentUser, WorkspaceRole.OWNER);

        if (request.getRole() == WorkspaceRole.OWNER) {
            throw new BusinessRuleException("Cannot assign OWNER role. User Ownership transfer");
        }

        WorkspaceMember member = workspaceMemberRepository
                .findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in this workspace"));

        if (member.getRole() == WorkspaceRole.OWNER) {
            throw new BusinessRuleException("Cannot change the owner's role. Transfer Ownership first");
        }

        member.setRole(request.getRole());
        return WorkspaceResponse.MemberResponse.from(member);
    }

    @Transactional
    public void removeMember(UUID workspaceId, UUID userId) {
        User currentUser = userService.resolveCurrentUser();
        Workspace workspace = findWorkspaceOrThrow(workspaceId);
        requireRole(workspace, currentUser, WorkspaceRole.ADMIN);

        WorkspaceMember target = workspaceMemberRepository
                .findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in this workspace"));

        if (target.getRole() == WorkspaceRole.OWNER) {
            throw new BusinessRuleException("Cannot remove the workspace owner");
        }

        workspace.getMembers().remove(target);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceResponse.MemberResponse> getMembers(UUID workspaceId) {
        Workspace workspace = findWorkspaceOrThrow(workspaceId);
        requireMember(workspace, userService.resolveCurrentUser());

        return workspace.getMembers()
                .stream()
                .map(WorkspaceResponse.MemberResponse::from)
                .toList();
    }

    private Workspace findWorkspaceOrThrow(UUID workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace", workspaceId));
    }

    private void requireMember(Workspace workspace, User user) {
        boolean isMember = workspace.getMembers()
                .stream()
                .anyMatch(m -> m.getUser().getId().equals(user.getId()));

        if (!isMember) {
            throw new AccessDeniedException("You are not a member of this workspace");
        }
    }

    private void requireRole(Workspace workspace, User user, WorkspaceRole minimumRole) {
        WorkspaceMember member = workspace.getMembers()
                .stream()
                .filter(m -> m.getUser().getId().equals(user.getId()))
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this workspace"));

        if (member.getRole().ordinal() > minimumRole.ordinal()) {
            throw new AccessDeniedException("Insufficient role. Required: " + minimumRole);
        }
    }

}
