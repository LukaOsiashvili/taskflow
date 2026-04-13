package dev.taskflow.project;

import dev.taskflow.common.response.ApiResponse;
import dev.taskflow.project.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/workspacess/{workspaceId}/projects", produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    //=====PROJECT MANAGEMENT=====

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED) //201
    public ApiResponse<ProjectResponse> createProject(@PathVariable UUID workspaceId, @Validated @RequestBody CreateProjectRequest request){
        return ApiResponse.success("Project created successfully", projectService.createProject(workspaceId, request));
    }

    @GetMapping
    public ApiResponse<List<ProjectResponse>> listProjects(@PathVariable UUID workspaceId) {
        return ApiResponse.success(projectService.listProjects(workspaceId));
    }

    @GetMapping("/{projectId}")
    public ApiResponse<ProjectResponse> getProject(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId
    ) {
        return ApiResponse.success(projectService.getProject(workspaceId, projectId));
    }

    @PatchMapping(value = "/{projectId}", consumes = APPLICATION_JSON_VALUE)
    public ApiResponse<ProjectResponse> updateProject(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @Validated @RequestBody UpdateProjectRequest request
    ) {
        return ApiResponse.success("Project updated successfully",
                projectService.updateProject(workspaceId, projectId, request));
    }

    @PatchMapping(value = "/{projectId}/status", consumes = APPLICATION_JSON_VALUE)
    public ApiResponse<ProjectResponse> changeStatus(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @Validated @RequestBody ChangeProjectStatusRequest request
    ) {
        return ApiResponse.success("Project status updated",
                projectService.changeStatus(workspaceId, projectId, request));
    }

    @DeleteMapping("/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId
    ) {
        projectService.deleteProject(workspaceId, projectId);
    }

    //=====PROJECT MEMBER MANAGEMENT=====

    @PostMapping(value = "/{projectId}/members", consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProjectResponse> inviteMember(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @Validated @RequestBody InviteProjectMemberRequest request
    ) {
        return ApiResponse.success("Member added to project",
                projectService.inviteMember(workspaceId, projectId, request));
    }

    @PatchMapping(value = "/{projectId}/members/{memberId}/role", consumes = APPLICATION_JSON_VALUE)
    public ApiResponse<ProjectResponse> updateMemberRole(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @PathVariable UUID memberId,
            @Validated @RequestBody UpdateProjectMemberRoleRequest request
    ) {
        return ApiResponse.success("Member role updated",
                projectService.updateMemberRole(workspaceId, projectId, memberId, request));
    }

    @DeleteMapping("/{projectId}/members/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @PathVariable UUID memberId
    ) {
        projectService.removeMember(workspaceId, projectId, memberId);
    }
}
