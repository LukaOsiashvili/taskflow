package dev.taskflow.task;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskTagRepository extends JpaRepository<TaskTag, TaskTagId> {
    List<TaskTag> findByTaskId(UUID taskId);
    boolean existsByTaskIdAndTagId(UUID taskId, UUID tagId);
    void deleteByTaskIdAndTagId(UUID taskId, UUID tagId);
}
