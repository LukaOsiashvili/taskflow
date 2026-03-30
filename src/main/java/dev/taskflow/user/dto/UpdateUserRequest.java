package dev.taskflow.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {

    @NotBlank(message = "Full name is required")

    @Size(max = 255,  message = "Full name must not exceed 255 characters")
    private String fullName;
}
