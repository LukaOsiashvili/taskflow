package dev.taskflow.tag;

import dev.taskflow.common.exception.AccessDeniedException;
import dev.taskflow.common.exception.BusinessRuleException;
import dev.taskflow.common.exception.ResourceNotFoundException;
import dev.taskflow.project.ProjectMemberRepository;
import dev.taskflow.project.ProjectRepository;
import dev.taskflow.tag.dto.CreateTagRequest;
import dev.taskflow.tag.dto.TagResponse;
import dev.taskflow.tag.dto.UpdateTagRequest;
import dev.taskflow.task.*;
import dev.taskflow.user.User;
import dev.taskflow.user.UserService;
import dev.taskflow.workspace.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final TaskTagRepository taskTagRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;
    private final UserService userService;

    //=====TAG=====

    @Transactional
    public TagResponse createTag(UUID workspaceId, CreateTagRequest request){
        User currentUser = userService.resolveCurrentUser();
        Workspace workspace = resolveWorkspace(workspaceId);
        WorkspaceMember member = resolveWorkspaceMember(workspaceId, currentUser);

        requireWorkspaceRole(member, WorkspaceRole.ADMIN); //Only admin can create tags

        if(tagRepository.existsByWorkspaceIdAndName(workspaceId, request.getName())){
            throw new BusinessRuleException("A tag with name '" + request.getName() +"' already exists in this workspace");
        }

        Tag tag = Tag.builder()
                .workspace(workspace)
                .name(request.getName())
                .color(request.getColor() != null ? request.getColor() : "6366f1")
                .build();

        tagRepository.save(tag);
        tagRepository.flush();

        return TagResponse.from(tag);
    }

    @Transactional(readOnly = true)
    public List<TagResponse> listTags(UUID workspaceId) {
        User currentUser = userService.resolveCurrentUser();
        resolveWorkspace(workspaceId);
        resolveWorkspaceMember(workspaceId, currentUser); // any member can list tags

        return tagRepository.findByWorkspaceId(workspaceId)
                .stream()
                .map(TagResponse::from)
                .toList();
    }

    @Transactional
    public TagResponse updateTag(UUID workspaceId, UUID tagId, UpdateTagRequest request){
        User currentUser = userService.resolveCurrentUser();
        resolveWorkspace(workspaceId);
        WorkspaceMember member = resolveWorkspaceMember(workspaceId, currentUser);
        requireWorkspaceRole(member, WorkspaceRole.ADMIN);

        Tag tag = resolveTagInWorkspace(tagId, workspaceId);

        if(request.getName() != null){
            if(!tag.getName().equals(request.getName()) && tagRepository.existsByWorkspaceIdAndName(workspaceId, request.getName())) {
                throw new BusinessRuleException("A tag with name '" + request.getName() +"' already exists in this workspace");
            }

            tag.setName(request.getName());
        }

        if(request.getColor() != null){
            tag.setColor(request.getColor());
        }

        return TagResponse.from(tag);
    }

    @Transactional
    public void deleteTag(UUID workspaceId, UUID tagId){
        User currentUser = userService.resolveCurrentUser();
        resolveWorkspace(workspaceId);
        WorkspaceMember member = resolveWorkspaceMember(workspaceId, currentUser);
        requireWorkspaceRole(member, WorkspaceRole.ADMIN);

        Tag tag = resolveTagInWorkspace(tagId, workspaceId);

        tagRepository.delete(tag);
    }

    //=====TASK TAG ASSIGNMENT=====
    //For tasks, anyone can assign and take tags from tasks

    @Transactional
    public List<TagResponse> addTagToTask(UUID workspaceId, UUID projectId, UUID taskId, UUID tagId){
        User currentUser = userService.resolveCurrentUser();
        resolveWorkspace(workspaceId);
        resolveWorkspaceMember(workspaceId, currentUser);
        resolveProjectInWorkspace(projectId, workspaceId);
        resolveProjectMember(projectId, currentUser);

        Task task = resolveTaskInProject(taskId, projectId);
        Tag tag = resolveTagInWorkspace(tagId, workspaceId);

        if(taskTagRepository.existsByTaskIdAndTagId(taskId, tagId)){
            throw new BusinessRuleException("Tag is already assigned to this task");
        }

        TaskTag taskTag = TaskTag.builder()
                .id(new TaskTagId(taskId, tagId))
                .task(task)
                .tag(tag)
                .build();

        taskTagRepository.save(taskTag);

        return getTagsForTask(taskId);
    }

    @Transactional
    public List<TagResponse> removeTagFromTask(UUID workspaceId, UUID projectId, UUID taskId, UUID tagId){
        User currentUser = userService.resolveCurrentUser();
        resolveWorkspace(workspaceId);
        resolveWorkspaceMember(workspaceId, currentUser);
        resolveProjectInWorkspace(projectId, workspaceId);
        resolveProjectMember(projectId, currentUser);

        resolveTaskInProject(taskId, projectId);
        resolveTagInWorkspace(tagId, workspaceId);

        if(!taskTagRepository.existsByTaskIdAndTagId(taskId, tagId)){
            throw new ResourceNotFoundException("Tag is not assigned to this task");
        }

        taskTagRepository.deleteByTaskIdAndTagId(taskId, tagId);

        return getTagsForTask(taskId);
    }

    @Transactional(readOnly = true)
    public List<TagResponse> getTaskTags(UUID workspaceId, UUID projectId, UUID taskId){
        User currentUser = userService.resolveCurrentUser();
        resolveWorkspace(workspaceId);
        resolveWorkspaceMember(workspaceId, currentUser);
        resolveProjectInWorkspace(projectId, workspaceId);
        resolveProjectMember(projectId, currentUser);
        resolveTaskInProject(taskId, projectId);

        return getTagsForTask(taskId);
    }

    // ===== PRIVATE HELPERS =====

    private List<TagResponse> getTagsForTask(UUID taskId) {
        return taskTagRepository.findByTaskId(taskId)
                .stream()
                .map(tt -> TagResponse.from(tt.getTag()))
                .toList();
    }

    private Workspace resolveWorkspace(UUID workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace", workspaceId));
    }

    private WorkspaceMember resolveWorkspaceMember(UUID workspaceId, User user) {
        return workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, user.getId())
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this workspace"));
    }

    private Tag resolveTagInWorkspace(UUID tagId, UUID workspaceId) {
        if (!tagRepository.existsByIdAndWorkspaceId(tagId, workspaceId)) {
            throw new ResourceNotFoundException("Tag", tagId);
        }
        return tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", tagId));
    }

    private void resolveProjectInWorkspace(UUID projectId, UUID workspaceId) {
        if (!projectRepository.existsByIdAndWorkspaceId(projectId, workspaceId)) {
            throw new ResourceNotFoundException("Project", projectId);
        }
    }

    private void resolveProjectMember(UUID projectId, User user) {
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, user.getId())) {
            throw new AccessDeniedException("You are not a member of this project");
        }
    }

    private Task resolveTaskInProject(UUID taskId, UUID projectId) {
        if (!taskRepository.existsByIdAndProjectId(taskId, projectId)) {
            throw new ResourceNotFoundException("Task", taskId);
        }
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
    }

    private void requireWorkspaceRole(WorkspaceMember member, WorkspaceRole minimumRole) {
        if (member.getRole().ordinal() > minimumRole.ordinal()) {
            throw new AccessDeniedException("This action requires workspace role: " + minimumRole);
        }
    }
}
