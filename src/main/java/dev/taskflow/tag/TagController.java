package dev.taskflow.tag;

import dev.taskflow.common.response.ApiResponse;
import dev.taskflow.tag.dto.CreateTagRequest;
import dev.taskflow.tag.dto.TagResponse;
import dev.taskflow.tag.dto.UpdateTagRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/workspaces/{workspaceId}/tags", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED) //201
    public ApiResponse<TagResponse> createTag(
            @PathVariable UUID workspaceId,
            @Validated @RequestBody CreateTagRequest request
    ) {
        return ApiResponse.success("Tag created successfully", tagService.createTag(workspaceId, request));
    }

    @GetMapping
    public ApiResponse<List<TagResponse>> listTags(@PathVariable UUID workspaceId) {
        return ApiResponse.success(tagService.listTags(workspaceId));
    }

    @PatchMapping(value = "/{tagId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<TagResponse> updateTag(
            @PathVariable UUID workspaceId,
            @PathVariable UUID tagId,
            @Validated @RequestBody UpdateTagRequest request
    ) {
        return ApiResponse.success("Tag updated successfully", tagService.updateTag(workspaceId, tagId, request));
    }

    @DeleteMapping("/{tagId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) //204
    public void deleteTag(
            @PathVariable UUID workspaceId,
            @PathVariable UUID tagId
    ){
        tagService.deleteTag(workspaceId, tagId);
    }
}
