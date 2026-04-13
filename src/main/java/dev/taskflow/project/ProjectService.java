package dev.taskflow.project;

import dev.taskflow.common.exception.AccessDeniedException;
import dev.taskflow.common.exception.BusinessRuleException;
import dev.taskflow.common.exception.ResourceNotFoundException;
import dev.taskflow.project.dto.*;
import dev.taskflow.user.User;
import dev.taskflow.user.UserRepository;
import dev.taskflow.user.UserService;
import dev.taskflow.workspace.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Transactional
    public ProjectResponse createProject(UUID workspaceId, CreateProjectRequest request){
        User currentUser = userService.resolveCurrentUser();
        Workspace workspace = resolveWorkspace(workspaceId);

        WorkspaceMember wsMember = resolveWorkspaceMember(workspace, currentUser);
        requireWorkspaceRole(wsMember, WorkspaceRole.ADMIN);

        Project project = Project.builder()
                .workspace(workspace)
                .name(request.getName())
                .description(request.getDescription())
                .status(ProjectStatus.PLANNING)
                .startDate(request.getStartDate())
                .dueDate(request.getDueDate())
                .build();

        projectRepository.save(project);

        ProjectMember creatorMember = ProjectMember.builder()
                .project(project)
                .user(currentUser)
                .role(ProjectRole.MANAGER)
                .build();

        project.getMembers().add(creatorMember);

        projectRepository.flush();

        log.debug("Project created: {} in workspace {}", project.getId(), workspaceId);
        return ProjectResponse.from(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listProjects(UUID workspaceId){
        User currentUser = userService.resolveCurrentUser();
        Workspace workspace = resolveWorkspace(workspaceId);

        resolveWorkspaceMember(workspace, currentUser);

        return projectRepository.findByWorkspaceId(workspaceId)
                .stream()
                .map(ProjectResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProject(UUID workspaceId, UUID projectId){
        User currentUser = userService.resolveCurrentUser();
        Workspace workspace = resolveWorkspace(workspaceId);

        resolveWorkspaceMember(workspace, currentUser);

        Project project = resolveProjectInWorkspace(projectId, workspaceId);

        return ProjectResponse.from(project);
    }

    @Transactional
    public ProjectResponse updateProject(UUID workspaceId, UUID projectId, UpdateProjectRequest request){
        User currentUser = userService.resolveCurrentUser();
        Workspace workspace = resolveWorkspace(workspaceId);

        resolveWorkspaceMember(workspace, currentUser);

        Project project = resolveProjectInWorkspace(projectId, workspaceId);

        ProjectMember projectMember = resolveProjectMember(project, currentUser);
        requireProjectRole(projectMember, ProjectRole.MANAGER);

        if(request.getName() != null){
            project.setName(request.getName());
        }
        if(request.getDescription() != null){
            project.setDescription(request.getDescription());
        }
        if(request.getStartDate() != null){
            project.setStartDate(request.getStartDate());
        }
        if(request.getDueDate() != null){
            project.setDueDate(request.getDueDate());
        }

        projectRepository.flush();

        return ProjectResponse.from(project);
    }

    @Transactional
    public ProjectResponse changeStatus(UUID workspaceId, UUID projectId, ChangeProjectStatusRequest request){
        User currentUser = userService.resolveCurrentUser();
        Workspace workspace = resolveWorkspace(workspaceId);

        resolveWorkspaceMember(workspace, currentUser);

        Project project = resolveProjectInWorkspace(projectId, workspaceId);

        ProjectMember projectMember = resolveProjectMember(project, currentUser);
        requireProjectRole(projectMember, ProjectRole.MANAGER);

        project.setStatus(request.getStatus());
        projectRepository.flush();

        log.debug("Project {} status changed to {}", projectId, request.getStatus());

        return ProjectResponse.from(project);
    }

    @Transactional
    public void deleteProject(UUID workspaceId, UUID projectId){
        User currentUser = userService.resolveCurrentUser();
        Workspace workspace = resolveWorkspace(workspaceId);

        WorkspaceMember wsMember = resolveWorkspaceMember(workspace, currentUser);
        requireWorkspaceRole(wsMember, WorkspaceRole.ADMIN);

        Project project = resolveProjectInWorkspace(projectId, workspaceId);

        projectRepository.delete(project);
        log.debug("Project {} deleted from workspace {}", projectId, workspaceId);
    }

    //=====PROJECT MEMBER MANAGEMENT=====

    @Transactional
    public ProjectResponse inviteMember(UUID workspaceId, UUID projectId, InviteProjectMemberRequest request){
        User currentUser = userService.resolveCurrentUser();
        Workspace workspace = resolveWorkspace(workspaceId);

        resolveWorkspaceMember(workspace, currentUser);

        Project project = resolveProjectInWorkspace(projectId, workspaceId);

        ProjectMember requesterMember = resolveProjectMember(project, currentUser);
        requireProjectRole(requesterMember, ProjectRole.MANAGER);

        User targetUser = userRepository.findById(request.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));

        boolean targetIsWorkspaceMember = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, targetUser.getId()).isPresent();

        if(!targetIsWorkspaceMember){
            throw new BusinessRuleException("User must be a workspace member before being added to a project");
        }

        if(projectMemberRepository.existsByProjectIdAndUserId(projectId, targetUser.getId())){
            throw new BusinessRuleException("User is already a member of this project");
        }

        ProjectMember newMember = ProjectMember.builder()
                .project(project)
                .user(targetUser)
                .role(request.getRole())
                .build();

        project.getMembers().add(newMember);
        projectRepository.flush();

        return ProjectResponse.from(project);
    }

    @Transactional
    public ProjectResponse updateMemberRole(UUID workspaceId, UUID projectId, UUID memberId, UpdateProjectMemberRoleRequest request){

        User currentUser = userService.resolveCurrentUser();
        Workspace workspace = resolveWorkspace(workspaceId);

        resolveWorkspaceMember(workspace, currentUser);

        Project project = resolveProjectInWorkspace(projectId, workspaceId);

        ProjectMember requesterMember = resolveProjectMember(project, currentUser);
        requireProjectRole(requesterMember, ProjectRole.MANAGER);

        ProjectMember targetMember = projectMemberRepository.findById(memberId)
                .filter(pm -> pm.getProject().getId().equals(projectId))
                .orElseThrow(() -> new ResourceNotFoundException("Project member", memberId));

        if (targetMember.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessRuleException("You cannot change your own project role");
        }

        targetMember.setRole(request.getRole());
        projectRepository.flush();

        return ProjectResponse.from(project);
    }

    @Transactional
    public ProjectResponse removeMember(UUID workspaceId, UUID projectId, UUID memberId){
        User currentUser = userService.resolveCurrentUser();
        Workspace workspace = resolveWorkspace(workspaceId);

        resolveWorkspaceMember(workspace, currentUser);

        Project project = resolveProjectInWorkspace(projectId, workspaceId);

        ProjectMember requesterMember = resolveProjectMember(project, currentUser);
        requireProjectRole(requesterMember, ProjectRole.MANAGER);

        ProjectMember targetMember = projectMemberRepository.findById(memberId)
                .filter(pm -> pm.getProject().getId().equals(projectId))
                .orElseThrow(() -> new ResourceNotFoundException("Project member", memberId));

        if (targetMember.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessRuleException("You cannot remove yourself from the project");
        }

        project.getMembers().remove(targetMember);
        projectRepository.flush();

        return ProjectResponse.from(project);
    }

    //=====PRIVATE HELPERS=====

    private Workspace resolveWorkspace(UUID workspaceId){
        return workspaceRepository.findById(workspaceId).orElseThrow(() -> new ResourceNotFoundException("Workspace", workspaceId));
    }

    private WorkspaceMember resolveWorkspaceMember(Workspace workspace, User user){
        return workspaceMemberRepository.findByWorkspaceIdAndUserId(workspace.getId(), user.getId()).orElseThrow(() -> new AccessDeniedException("You are not a member of this workspace"));
    }

    private Project resolveProjectInWorkspace(UUID projectId, UUID workspaceId){
        if(!projectRepository.existsByIdAndWorkspaceId(projectId, workspaceId)){
            throw new ResourceNotFoundException("Project", projectId);
        }

        return projectRepository.findByIdWithWorkspace(projectId).orElseThrow(() -> new ResourceNotFoundException("Project", projectId));
    }

    private ProjectMember resolveProjectMember(Project project, User user){
        return projectMemberRepository.findByProjectIdAndUserId(project.getId(), user.getId()).orElseThrow(() -> new AccessDeniedException("You are not a member of this project"));
    }

    private void requireWorkspaceRole(WorkspaceMember member, WorkspaceRole minimumRole){
        if(member.getRole().ordinal() > minimumRole.ordinal()){
            throw new AccessDeniedException("This action required workspace role: " + minimumRole);
        }
    }

    private void requireProjectRole(ProjectMember member, ProjectRole minimumRole){
        if(member.getRole().ordinal() > minimumRole.ordinal()){
            throw new AccessDeniedException("This action required project role: " + minimumRole);
        }
    }
}
