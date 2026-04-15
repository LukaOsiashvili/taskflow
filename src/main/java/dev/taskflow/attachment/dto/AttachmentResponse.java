package dev.taskflow.attachment.dto;

import dev.taskflow.attachment.Attachment;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
public class AttachmentResponse {

    private final UUID id;
    private final UUID taskId;
    private final UploaderInfo uploadedBy;
    private final String fileName;
    private final String fileUrl;
    private final Integer fileSizeBytes;
    private final OffsetDateTime uploadedAt;

    private AttachmentResponse(Attachment attachment){
        this.id = attachment.getId();
        this.taskId = attachment.getTask().getId();
        this.uploadedBy = attachment.getUploadedBy() != null
                ? new UploaderInfo(
                        attachment.getUploadedBy().getId(),
                        attachment.getUploadedBy().getFullName()
                        )
                : null;
        this.fileName = attachment.getFileName();
        this.fileUrl = attachment.getFileUrl();
        this.fileSizeBytes = attachment.getFileSizeBytes();
        this.uploadedAt = attachment.getUploadedAt();
    }

    public static AttachmentResponse from(Attachment attachment){
        return new AttachmentResponse(attachment);
    }

    public record UploaderInfo(UUID id, String fullName) {}

}
