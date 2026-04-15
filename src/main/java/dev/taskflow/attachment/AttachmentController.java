package dev.taskflow.attachment;

import dev.taskflow.attachment.dto.AttachmentResponse;
import dev.taskflow.attachment.dto.CreateAttachmentRequest;
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
        value = "/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}/attachments",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED) //201
    public ApiResponse<AttachmentResponse> addAttachment(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @Validated @RequestBody CreateAttachmentRequest request
    ) {
        return ApiResponse.success("Attachment added", attachmentService.addAttachment(workspaceId, projectId, taskId, request));
    }

    @GetMapping
    public ApiResponse<List<AttachmentResponse>> listAttachments(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @PathVariable UUID taskId
    ) {
        return ApiResponse.success(attachmentService.listAttachment(workspaceId, projectId, taskId));
    }

    @DeleteMapping("/{attachmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) //204
    public void deleteAttachment(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @PathVariable UUID attachmentId
    ) {
        attachmentService.deleteAttachment(workspaceId, projectId, taskId, attachmentId);
    }

}
