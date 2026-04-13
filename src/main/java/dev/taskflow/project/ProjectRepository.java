package dev.taskflow.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {


    List<Project> findByWorkspaceId(UUID  workspaceId);

    boolean existsByIdAndWorkspaceId(UUID id, UUID workspaceId);

    @Query("SELECT p from Project p JOIN FETCH p.workspace WHERE p.id = :id")
    Optional<Project> findByIdWithWorkspace(@Param("id") UUID id);
}
