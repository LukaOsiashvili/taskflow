package dev.taskflow.user;

import dev.taskflow.common.exception.ResourceNotFoundException;
import dev.taskflow.user.dto.UpdateUserRequest;
import dev.taskflow.user.dto.UserResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getCurrentUser(){
        User user = resolveCurrentUser();
        return UserResponse.from(user);
    }

    public UserResponse getUserById(UUID id){
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateCurrentUser(UpdateUserRequest request)
    {
        User user = resolveCurrentUser();
        user.setFullName(request.getFullName());

        return UserResponse.from(user);
    }

    public User resolveCurrentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found in database"));
    }

}
