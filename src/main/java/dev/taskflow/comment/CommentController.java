package dev.taskflow.comment;

import dev.taskflow.comment.dto.CommentResponse;
import dev.taskflow.comment.dto.CreateCommentRequest;
import dev.taskflow.comment.dto.UpdateCommentRequest;
import dev.taskflow.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(
        value = "/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}/comments",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED) //201
    public ApiResponse<CommentResponse> createComment(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @Validated @RequestBody CreateCommentRequest request
    ) {
        return ApiResponse.success("Comment added", commentService.createComment(workspaceId, projectId, taskId, request));
    }

    @GetMapping
    public ApiResponse<List<CommentResponse>> listComments(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @PathVariable UUID taskId
    ) {
        return ApiResponse.success(commentService.listComments(workspaceId, projectId, taskId));
    }

    @PutMapping(value = "/{commentId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<CommentResponse> updateComment(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @PathVariable UUID commentId,
            @Validated @RequestBody UpdateCommentRequest request
    ) {
        return ApiResponse.success("Comment updated", commentService.updateComment(workspaceId, projectId, taskId, commentId, request));
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) //204
    public void deleteComment(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @PathVariable UUID commentId
    ){
        commentService.deleteComment(workspaceId, projectId, taskId, commentId);
    }
}
