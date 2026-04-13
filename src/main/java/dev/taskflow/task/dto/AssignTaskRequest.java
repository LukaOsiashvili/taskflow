package dev.taskflow.task.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AssignTaskRequest {

    private UUID assigneeId; //Nullable: Passing null unassigns the task | unassign is valid operation via this endpoint
}
