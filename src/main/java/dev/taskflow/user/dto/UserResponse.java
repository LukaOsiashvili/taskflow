package dev.taskflow.user.dto;

import dev.taskflow.user.GlobalRole;
import dev.taskflow.user.User;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
public class UserResponse {

    private final UUID id;
    private final String email;
    private final String fullName;
    private final GlobalRole globalRole;
    private final OffsetDateTime createdAt;

    public static UserResponse from(User user){
        return new UserResponse(user);
    }

    private UserResponse(User user){
        this.id = user.getId();
        this.email = user.getEmail();
        this.fullName = user.getFullName();
        this.globalRole = user.getGlobalRole();
        this.createdAt = user.getCreatedAt();
    }

}
