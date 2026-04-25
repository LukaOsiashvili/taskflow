package dev.taskflow.common.actuator;

import dev.taskflow.project.ProjectRepository;
import dev.taskflow.task.TaskRepository;
import dev.taskflow.user.UserRepository;
import dev.taskflow.workspace.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Endpoint(id = "taskflow-stats")
@RequiredArgsConstructor
public class TaskflowStatsEndpoint {

    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    @ReadOperation //HTTP GET
    public Map<String, Object> stats(){
        return Map.of(
                "users", userRepository.count(),
                "workspaces", workspaceRepository.count(),
                "projects", projectRepository.count(),
                "tasks", taskRepository.count()
        );
    }
}
