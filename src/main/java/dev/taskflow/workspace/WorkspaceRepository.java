package dev.taskflow.workspace;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {

    Optional<Workspace> findBySlug(String slug);


    boolean existsBySlug(String slug);

    List<Workspace> findAllByOwnerId(UUID ownerId);
}
