package com.cuscatlan.coworking.service;

import com.cuscatlan.coworking.dto.request.LoginRequest;
import com.cuscatlan.coworking.dto.request.RegisterRequest;
import com.cuscatlan.coworking.dto.response.AuthResponse;
import com.cuscatlan.coworking.entity.Role;
import com.cuscatlan.coworking.entity.User;
import com.cuscatlan.coworking.exception.EmailAlreadyInUseException;
import com.cuscatlan.coworking.repository.UserRepository;
import com.cuscatlan.coworking.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyInUseException(request.email());
        }

        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return AuthResponse.of(token, user.getEmail(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado"));

        String token = jwtService.generateToken(user);
        return AuthResponse.of(token, user.getEmail(), user.getRole().name());
    }
}
