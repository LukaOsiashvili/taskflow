package dev.taskflow.comment.dto;

import dev.taskflow.comment.Comment;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
public class CommentResponse {

    private final UUID id;

    private final UUID taskId;
    private final AuthorInfo author;
    private final String body;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime editedAt; //null if edited never

    private CommentResponse(Comment comment) {
        this.id = comment.getId();
        this.taskId = comment.getTask().getId();
        this.author = comment.getAuthor() != null
                ? new AuthorInfo(
                    comment.getAuthor().getId(),
                    comment.getAuthor().getFullName(),
                    comment.getAuthor().getEmail()
                    )
                : null;
        this.body = comment.getBody();
        this.createdAt = comment.getCreatedAt();
        this.editedAt = comment.getEditedAt();
    }

    public static CommentResponse from(Comment comment){
        return new CommentResponse(comment);
    }

    public record AuthorInfo(UUID id, String fullName, String email) {}

}
