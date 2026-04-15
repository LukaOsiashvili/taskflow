package dev.taskflow.comment;

import dev.taskflow.comment.dto.CommentResponse;
import dev.taskflow.comment.dto.CreateCommentRequest;
import dev.taskflow.comment.dto.UpdateCommentRequest;
import dev.taskflow.common.exception.AccessDeniedException;
import dev.taskflow.common.exception.ResourceNotFoundException;
import dev.taskflow.project.ProjectMemberRepository;
import dev.taskflow.project.ProjectRepository;
import dev.taskflow.task.Task;
import dev.taskflow.task.TaskRepository;
import dev.taskflow.user.User;
import dev.taskflow.user.UserService;
import dev.taskflow.workspace.WorkspaceMemberRepository;
import dev.taskflow.workspace.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserService userService;

    @Transactional
    public CommentResponse createComment(UUID workspaceId, UUID projectId, UUID taskId, CreateCommentRequest request){
        User currentUser = userService.resolveCurrentUser();

        resolveWorkspaceMembership(workspaceId, currentUser);
        resolveProjectInWorkspace(projectId, workspaceId);
        resolveProjectMembership(projectId, currentUser);
        Task task = resolveTaskInProject(taskId, projectId);

        Comment comment = Comment.builder()
                .task(task)
                .author(currentUser)
                .body(request.getBody())
                .build();

        commentRepository.save(comment);
        commentRepository.flush();

        return CommentResponse.from(comment);

    }

    @Transactional(readOnly = true)
    public List<CommentResponse> listComments(UUID workspaceId, UUID projectId, UUID taskId){
        User currentUser = userService.resolveCurrentUser();

        resolveWorkspaceMembership(workspaceId, currentUser);
        resolveProjectInWorkspace(projectId, workspaceId);
        resolveProjectMembership(projectId, currentUser);
        resolveTaskInProject(taskId, projectId);

        return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId)
                .stream()
                .map(CommentResponse::from)
                .toList();
    }

    @Transactional
    public CommentResponse updateComment(UUID workspaceId, UUID projectId, UUID taskId, UUID commentId, UpdateCommentRequest request){
        User currentUser = userService.resolveCurrentUser();

        resolveWorkspaceMembership(workspaceId, currentUser);
        resolveProjectInWorkspace(projectId, workspaceId);
        resolveProjectMembership(projectId, currentUser);
        resolveTaskInProject(taskId, projectId);

        Comment comment = resolveCommentInTask(commentId, taskId);
        requireCommentAuthor(comment, currentUser);

        comment.setBody(request.getBody());

        comment.setEditedAt(OffsetDateTime.now());

        return CommentResponse.from(comment);
    }

    @Transactional
    public void deleteComment(UUID workspaceId, UUID projectId, UUID taskId, UUID commentId){
        User currentUser = userService.resolveCurrentUser();

        resolveWorkspaceMembership(workspaceId, currentUser);
        resolveProjectInWorkspace(projectId, workspaceId);
        resolveProjectMembership(projectId, currentUser);
        resolveTaskInProject(taskId, projectId);

        Comment comment = resolveCommentInTask(commentId, taskId);
        requireCommentAuthor(comment, currentUser);

        commentRepository.delete(comment);
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

    private Comment resolveCommentInTask(UUID commentId, UUID taskId) {
        if (!commentRepository.existsByIdAndTaskId(commentId, taskId)) {
            throw new ResourceNotFoundException("Comment", commentId);
        }
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));
    }

    private void requireCommentAuthor(Comment comment, User user) {
        if (comment.getAuthor() == null || !comment.getAuthor().getId().equals(user.getId())) {
            throw new AccessDeniedException("Only the comment author can perform this action");
        }
    }

}
