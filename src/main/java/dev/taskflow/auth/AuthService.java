package dev.taskflow.auth;

import dev.taskflow.auth.dto.AuthResponse;
import dev.taskflow.auth.dto.LoginRequest;
import dev.taskflow.auth.dto.RegisterRequest;
import dev.taskflow.common.exception.BusinessRuleException;
import dev.taskflow.common.security.JwtService;
import dev.taskflow.user.GlobalRole;
import dev.taskflow.user.User;
import dev.taskflow.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request){
        if (userRepository.existsByEmail(request.getEmail())){
            throw new BusinessRuleException("Email already in user");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .globalRole(GlobalRole.USER)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponse(token, user.getId(), user.getEmail(), user.getFullName());
    }

    public AuthResponse login(LoginRequest request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new BusinessRuleException("User not found"));

        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponse(token, user.getId(), user.getEmail(), user.getFullName());
    }

}
