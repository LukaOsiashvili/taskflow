package dev.taskflow.task;

import dev.taskflow.common.response.ApiResponse;
import dev.taskflow.task.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(
        value = "/workspace/{workspaceId}/projects/{projectId}/tasks",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED) //201
    public ApiResponse<TaskResponse> createTask(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @Validated @RequestBody CreateTaskRequest request
    ) {
        return ApiResponse.success("Task created successfully", taskService.createTask(workspaceId, projectId, request));
    }

    @GetMapping
    public ApiResponse<Page<TaskResponse>> listTasks(
            @PathVariable UUID workspaceID,
            @PathVariable UUID projectId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) UUID assigneeId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return ApiResponse.success(taskService.listTasks(workspaceID, projectId, status, priority, assigneeId, pageable));
    }

    @GetMapping("/{taskId}")
    public ApiResponse<TaskResponse> getTask(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @PathVariable UUID taskId
    ) {
        return ApiResponse.success(taskService.getTask(workspaceId, projectId, taskId));
    }

    @PatchMapping(value = "/{taskId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<TaskResponse> updateTask(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @Validated @RequestBody UpdateTaskRequest request
    ) {
        return ApiResponse.success("Task updated successfully", taskService.updateTask(workspaceId, projectId, taskId, request));
    }

    @PatchMapping(value = "/{taskId}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<TaskResponse> changeStatus(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @Validated @RequestBody ChangeTaskStatusRequest request
    ) {
        return ApiResponse.success("Task status update", taskService.changeStatus(workspaceId, projectId, taskId, request));
    }

    @PatchMapping(value = "/{taskId}/assignee", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<TaskResponse> assignTask(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @RequestBody AssignTaskRequest request
    ) {
        return ApiResponse.success("Task assigned", taskService.assignTask(workspaceId, projectId, taskId, request));
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) //204
    public void deleteTask(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @PathVariable UUID taskId
    ){
        taskService.deleteTask(workspaceId, projectId, taskId);
    }

    @GetMapping("/{taskId}/subtasks")
    public ApiResponse<List<TaskResponse>> getSubtasks(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @PathVariable UUID taskId
    ){
        return ApiResponse.success(taskService.getSubtasks(workspaceId, projectId, taskId));
    }

}
