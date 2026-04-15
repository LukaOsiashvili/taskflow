package dev.taskflow.attachment;

import dev.taskflow.attachment.dto.AttachmentResponse;
import dev.taskflow.attachment.dto.CreateAttachmentRequest;
import dev.taskflow.common.exception.AccessDeniedException;
import dev.taskflow.common.exception.ResourceNotFoundException;
import dev.taskflow.project.ProjectMemberRepository;
import dev.taskflow.project.ProjectRepository;
import dev.taskflow.project.ProjectRole;
import dev.taskflow.task.Task;
import dev.taskflow.task.TaskRepository;
import dev.taskflow.user.User;
import dev.taskflow.user.UserService;
import dev.taskflow.workspace.WorkspaceMemberRepository;
import dev.taskflow.workspace.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserService userService;

    @Transactional
    public AttachmentResponse addAttachment(UUID workspaceId, UUID projectId, UUID taskId, CreateAttachmentRequest request){

        User currentUser = userService.resolveCurrentUser();

        resolveWorkspaceMembership(workspaceId, currentUser);
        resolveProjectInWorkspace(projectId, workspaceId);
        resolveProjectMembership(projectId, currentUser);
        Task task = resolveTaskInProject(taskId, projectId);

        Attachment attachment = Attachment.builder()
                .task(task)
                .uploadedBy(currentUser)
                .fileName(request.getFileName())
                .fileUrl(request.getFileUrl())
                .fileSizeBytes(request.getFileSizeBytes())
                .build();

        attachmentRepository.save(attachment);
        attachmentRepository.flush();

        return AttachmentResponse.from(attachment);
    }

    @Transactional
    public List<AttachmentResponse> listAttachment(UUID workspaceId, UUID projectId, UUID taskId){
        User currentUser = userService.resolveCurrentUser();

        resolveWorkspaceMembership(workspaceId, currentUser);
        resolveProjectInWorkspace(projectId, workspaceId);
        resolveProjectMembership(projectId, currentUser);
        resolveTaskInProject(taskId, projectId);

        return attachmentRepository.findByTaskId(taskId)
                .stream()
                .map(AttachmentResponse::from)
                .toList();
    }

    @Transactional
    public void deleteAttachment(UUID workspaceId, UUID projectId, UUID taskId, UUID attachmentId){
        User currentUser = userService.resolveCurrentUser();

        resolveWorkspaceMembership(workspaceId, currentUser);
        resolveProjectInWorkspace(projectId, workspaceId);

        var projectMember = projectMemberRepository
                .findByProjectIdAndUserId(projectId, currentUser.getId())
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this project"));

        resolveTaskInProject(taskId, projectId);

        Attachment attachment = attachmentRepository.findById(attachmentId)
                .filter(a -> a.getTask().getId().equals(taskId))
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", attachmentId));

        boolean isManager = projectMember.getRole().ordinal() <= ProjectRole.MANAGER.ordinal();
        boolean isUploader = attachment.getUploadedBy() != null && attachment.getUploadedBy().getId().equals(currentUser.getId());

        if(!isManager && !isUploader){
            throw new AccessDeniedException("Only project managers or the uploader can delete attachment");
        }

        attachmentRepository.delete(attachment);

    }

    // ===== PRIVATE HELPERS =====

    private void resolveWorkspaceMembership(UUID workspaceId, User user) {
        workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace", workspaceId));
        workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, user.getId())
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this workspace"));
    }

    private void resolveProjectInWorkspace(UUID projectId, UUID workspaceId) {
        if (!projectRepository.existsByIdAndWorkspaceId(projectId, workspaceId)) {
            throw new ResourceNotFoundException("Project", projectId);
        }
    }

    private void resolveProjectMembership(UUID projectId, User user) {
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

}
