package dev.taskflow.tag;

import dev.taskflow.common.response.ApiResponse;
import dev.taskflow.tag.dto.TagResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping(
        value = "/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}/tags",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor
public class TaskTagController {

    private final TagService tagService;

    @GetMapping
    public ApiResponse<List<TagResponse>> getTaskTags(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @PathVariable UUID taskId
    ) {
        return ApiResponse.success(tagService.getTaskTags(workspaceId, projectId, taskId));
    }

    @PutMapping("/{tagId}")
    @ResponseStatus(HttpStatus.CREATED) //because of the idea that this endpoint literally means: "put this tag on this task"
    public ApiResponse<List<TagResponse>> addTagToTask(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @PathVariable UUID tagId
    ) {
        return ApiResponse.success("Tag added to task", tagService.addTagToTask(workspaceId, projectId, taskId, tagId));
    }

    @DeleteMapping("/{tagId}")
    @ResponseStatus(HttpStatus.OK) //200
    public ApiResponse<List<TagResponse>> removeTagFromTask(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @PathVariable UUID tagId
    ){
        return ApiResponse.success("Tag removed from task", tagService.removeTagFromTask(workspaceId, projectId, taskId, tagId));
    }

}
