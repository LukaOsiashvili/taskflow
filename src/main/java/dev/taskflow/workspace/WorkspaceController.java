package dev.taskflow.workspace;

import dev.taskflow.common.response.ApiResponse;
import dev.taskflow.workspace.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/workspaces", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED) // 201
    public ApiResponse<WorkspaceResponse> create(@Validated @RequestBody CreateWorkspaceRequest request){
        return ApiResponse.success("Workspace created successfully", workspaceService.create(request));
    }

    @GetMapping
    public ApiResponse<List<WorkspaceResponse>> getMyWorkspaces(){
        return ApiResponse.success(workspaceService.getMyWorkspaces());
    }

    @GetMapping("/{workspaceId}")
    public ApiResponse<WorkspaceResponse> getById(@PathVariable UUID workspaceId){
        return ApiResponse.success(workspaceService.getById(workspaceId));
    }

    @PutMapping(path = "/{workspaceId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<WorkspaceResponse> update(@PathVariable UUID workspaceId, @Validated @RequestBody UpdateWorkspaceRequest request){
        return ApiResponse.success("Workspace updated successfully", workspaceService.update(workspaceId, request));
    }

    @DeleteMapping("/{workspaceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) //204
    public void delete(@PathVariable UUID workspaceId){
        workspaceService.delete(workspaceId);
    }

    @GetMapping("/{workspaceId}/members")
    public ApiResponse<List<WorkspaceResponse.MemberResponse>> getMembers(@PathVariable UUID workspaceId){
        return ApiResponse.success(workspaceService.getMembers(workspaceId));
    }

    @PostMapping(path = "/{workspaceId}/members", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED) //201
    public ApiResponse<WorkspaceResponse.MemberResponse> inviteMember(@PathVariable UUID workspaceId, @Validated @RequestBody InviteMemberRequest request){
        return ApiResponse.success("Member invited successfully", workspaceService.inviteMember(workspaceId, request));
    }

    @PatchMapping(path = "/{workspaceId}/members/{userId}/role", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<WorkspaceResponse.MemberResponse> updateMemberRole(@PathVariable UUID workspaceId, @PathVariable UUID userId, @Validated @RequestBody UpdateMemberRoleRequest request){
        return ApiResponse.success("Role updated successfully", workspaceService.updateMemberRole(workspaceId, userId, request));
    }

    @DeleteMapping("/{workspaceId}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) //204
    public void removeMember(@PathVariable UUID workspaceId, @PathVariable UUID userId){
        workspaceService.removeMember(workspaceId, userId);
    }
}
