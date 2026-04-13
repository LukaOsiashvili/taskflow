package dev.taskflow.task;

import dev.taskflow.common.exception.AccessDeniedException;
import dev.taskflow.common.exception.BusinessRuleException;
import dev.taskflow.common.exception.ResourceNotFoundException;
import dev.taskflow.project.*;
import dev.taskflow.task.dto.*;
import dev.taskflow.user.User;
import dev.taskflow.user.UserRepository;
import dev.taskflow.user.UserService;
import dev.taskflow.workspace.Workspace;
import dev.taskflow.workspace.WorkspaceMember;
import dev.taskflow.workspace.WorkspaceMemberRepository;
import dev.taskflow.workspace.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    //=====TASK CRUD=====

    @Transactional
    public TaskResponse createTask(UUID workspaceId, UUID projectId, CreateTaskRequest request){
        User currentUser = userService.resolveCurrentUser();

        resolveWorkspace(workspaceId);
        resolveWorkspaceMember(workspaceId, currentUser);

        Project project = resolveProjectInWorkspace(projectId, workspaceId);
        resolveProjectMember(projectId, currentUser); //Any member can create task

        User assignee = null;
        if(request.getAssigneeId() != null){
            assignee = resolveUser(request.getAssigneeId());
            if(!projectMemberRepository.existsByProjectIdAndUserId(projectId, assignee.getId())){
                throw new BusinessRuleException("Assignee must be a member of the project");
            }
        }

        Task parentTask = null;
        if(request.getParentTaskId() != null){
            parentTask = taskRepository.findById(request.getParentTaskId()).orElseThrow(() -> new ResourceNotFoundException("Task", request.getParentTaskId()));
            if(!parentTask.getProject().getId().equals(projectId)){
                throw new BusinessRuleException("Parent task must belong to the same project");
            }
        }

        Task task = Task.builder()
                .project(project)
                .reporter(currentUser)
                .assignee(assignee)
                .parentTask(parentTask)
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM)
                .storyPoints(request.getStoryPoints())
                .dueDate(request.getDueDate())
                .build();

        taskRepository.save(task);
        taskRepository.flush();

        log.debug("Task created: {} in project {}", task.getId(), projectId);
        return TaskResponse.from(task);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> listTasks(
            UUID workspaceId,
            UUID projectId,
            TaskStatus status,
            TaskPriority priority,
            UUID assigneeId,
            Pageable pageable
    ){
        User currentUser = userService.resolveCurrentUser();

        resolveWorkspace(workspaceId);
        resolveWorkspaceMember(workspaceId, currentUser);
        resolveProjectInWorkspace(projectId, workspaceId);
        resolveProjectMember(projectId, currentUser);

        return taskRepository.findTopLevelTasks(projectId, status, priority, assigneeId, pageable).map(TaskResponse::from);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(UUID workspaceId, UUID projectId, UUID taskId){
        User currentUser = userService.resolveCurrentUser();

        resolveWorkspace(workspaceId);
        resolveWorkspaceMember(workspaceId, currentUser);
        resolveProjectInWorkspace(projectId, workspaceId);
        resolveProjectMember(projectId, currentUser);

        Task task = resolveTaskInProject(taskId, projectId);

        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse updateTask(UUID workspaceId, UUID projectId, UUID taskId, UpdateTaskRequest request){
        User currentUser = userService.resolveCurrentUser();

        resolveWorkspace(workspaceId);
        resolveWorkspaceMember(workspaceId, currentUser);
        resolveProjectInWorkspace(projectId, workspaceId);

        ProjectMember member = resolveProjectMember(projectId, currentUser);
        Task task = resolveTaskInProject(taskId, projectId);

        boolean isManager = member.getRole().ordinal() <= ProjectRole.MANAGER.ordinal();
        boolean isReporter = task.getReporter() != null && task.getReporter().getId().equals(currentUser.getId());

        if(!isManager && !isReporter){
            throw new AccessDeniedException("Only project managers or the task reported can edit task details");
        }

        if(request.getTitle() != null) task.setTitle(request.getTitle());
        if(request.getDescription() != null) task.setDescription(request.getDescription());
        if(request.getPriority() != null) task.setPriority(request.getPriority());
        if(request.getStoryPoints() != null) task.setStoryPoints(request.getStoryPoints());
        if(request.getDueDate() != null) task.setDueDate(request.getDueDate());

        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse changeStatus(UUID workspaceId, UUID projectId, UUID taskId, ChangeTaskStatusRequest request){
        User currentUser = userService.resolveCurrentUser();

        resolveWorkspace(workspaceId);
        resolveWorkspaceMember(workspaceId, currentUser);
        resolveProjectInWorkspace(projectId, workspaceId);

        resolveProjectMember(projectId, currentUser); // Any project member can change status

        Task task = resolveTaskInProject(taskId, projectId);
        task.setStatus(request.getStatus());

        log.debug("Task {} status changed to {}", taskId, request.getStatus());
        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse assignTask(UUID workspaceId, UUID projectId, UUID taskId, AssignTaskRequest request){
        User currentUser = userService.resolveCurrentUser();

        resolveWorkspace(workspaceId);
        resolveWorkspaceMember(workspaceId, currentUser);
        resolveProjectInWorkspace(projectId, workspaceId);

        ProjectMember member = resolveProjectMember(projectId, currentUser);
        requireProjectRole(member, ProjectRole.MANAGER); //Only managers can assign

        Task task = resolveTaskInProject(taskId, projectId);

        if(request.getAssigneeId() == null){
            task.setAssignee(null); // for null unassign the user
        } else{
            User assignee = resolveUser(request.getAssigneeId());
            if(!projectMemberRepository.existsByProjectIdAndUserId(projectId, assignee.getId())){
                throw new BusinessRuleException("Assignee must be a member of the project");
            }
            task.setAssignee(assignee);
        }

        return TaskResponse.from(task);
    }

    @Transactional
    public void deleteTask(UUID workspaceId, UUID projectId, UUID taskId){
        User currentUser = userService.resolveCurrentUser();

        resolveWorkspace(workspaceId);
        resolveWorkspaceMember(workspaceId, currentUser);
        resolveProjectInWorkspace(projectId, workspaceId);

        ProjectMember member = resolveProjectMember(projectId, currentUser);
        requireProjectRole(member, ProjectRole.MANAGER);

        Task task = resolveTaskInProject(taskId, projectId);

        taskRepository.delete(task);

        log.debug("Task {} deleted from project {}", taskId, projectId);
    }

    //=====SUBTASKS=====

    @Transactional(readOnly = true)
    public List<TaskResponse> getSubtasks(UUID workspaceId, UUID projectId, UUID taskId){
        User currentUser = userService.resolveCurrentUser();

        resolveWorkspace(workspaceId);
        resolveWorkspaceMember(workspaceId, currentUser);
        resolveProjectInWorkspace(projectId, workspaceId);
        resolveProjectMember(projectId, currentUser);

        resolveTaskInProject(taskId, projectId);

        return taskRepository.findByParentTaskId(taskId)
                .stream()
                .map(TaskResponse::from)
                .toList();
    }

    //=====PRIVATE HELPERS=====

    private Workspace resolveWorkspace(UUID workspaceId) {
        return workspaceRepository.findById(workspaceId).orElseThrow(() -> new ResourceNotFoundException("Workspace", workspaceId));
    }

    private WorkspaceMember resolveWorkspaceMember(UUID workspaceId, User user){
        return workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, user.getId()).orElseThrow(() -> new AccessDeniedException("You are not a member of this work space"));
    }

    private Project resolveProjectInWorkspace(UUID projectId, UUID workspaceId){
        if(!projectRepository.existsByIdAndWorkspaceId(projectId, workspaceId)){
            throw new ResourceNotFoundException("Project", projectId);
        }

        return projectRepository.findById(projectId).orElseThrow(() -> new ResourceNotFoundException("Project", projectId));
    }

    private ProjectMember resolveProjectMember(UUID projectId, User user){
        return projectMemberRepository.findByProjectIdAndUserId(projectId, user.getId()).orElseThrow(() -> new AccessDeniedException("You are not a member of this project"));
    }

    private Task resolveTaskInProject(UUID taskId, UUID projectId){
        if(!taskRepository.existsByIdAndProjectId(taskId, projectId)){
            throw new ResourceNotFoundException("Task", taskId);
        }
        return taskRepository.findById(taskId).orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
    }

    private User resolveUser(UUID userId){
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private void requireProjectRole(ProjectMember member, ProjectRole minimumRole){
        if(member.getRole().ordinal() > minimumRole.ordinal()){
            throw new AccessDeniedException("This action requires project role: " + minimumRole);
        }
    }

}
