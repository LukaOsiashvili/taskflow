package dev.taskflow.user;

import dev.taskflow.common.response.ApiResponse;
import dev.taskflow.user.dto.UpdateUserRequest;
import dev.taskflow.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(path = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserResponse> getCurrentUser(){
        return ApiResponse.success(userService.getCurrentUser());
    }

    @PutMapping(path = "/me", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<UserResponse> updateCurrentUser(@Validated @RequestBody UpdateUserRequest request){
        return ApiResponse.success("Profile updated successfully", userService.updateCurrentUser(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUserById(@PathVariable UUID id){
        return ApiResponse.success(userService.getUserById(id));
    }
}
