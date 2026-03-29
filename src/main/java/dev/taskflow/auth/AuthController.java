package dev.taskflow.auth;

import dev.taskflow.auth.dto.AuthResponse;
import dev.taskflow.auth.dto.LoginRequest;
import dev.taskflow.auth.dto.RegisterRequest;
import dev.taskflow.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponse> register(@Validated @RequestBody RegisterRequest request){
        return ApiResponse.success("User registered successfully", authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Validated @RequestBody LoginRequest request){
        return ApiResponse.success(authService.login(request));
    }
}
