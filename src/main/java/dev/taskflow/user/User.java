package dev.taskflow.user;

import dev.taskflow.workspace.Workspace;
import dev.taskflow.workspace.WorkspaceMember;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;


import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GlobalRole globalRole = GlobalRole.USER;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "owner")
    @Builder.Default
    private List<Workspace> ownedWorkspaces = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<WorkspaceMember> workspaceMembership = new ArrayList<>();
}
