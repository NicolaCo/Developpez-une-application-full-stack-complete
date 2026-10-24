package com.openclassrooms.mddapi.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.openclassrooms.mddapi.dto.auth.AuthResponse;
import com.openclassrooms.mddapi.dto.auth.LoginRequest;
import com.openclassrooms.mddapi.dto.auth.RegisterRequest;
import com.openclassrooms.mddapi.exception.EmailAlreadyUsedException;
import com.openclassrooms.mddapi.exception.UsernameAlreadyUsedException;
import com.openclassrooms.mddapi.model.User;
import com.openclassrooms.mddapi.repository.UserRepository;
import com.openclassrooms.mddapi.security.AuthUser;
import com.openclassrooms.mddapi.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyUsedException();
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyUsedException();
        }
        User user = new User(request.email(), request.username(), passwordEncoder.encode(request.password()));
        User saved = userRepository.save(user);
        String token = jwtService.generateToken(saved.getEmail());
        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        if (!(authentication.getPrincipal() instanceof AuthUser principal)) {
            throw new IllegalStateException("Unexpected authenticated principal");
        }
        return new AuthResponse(jwtService.generateToken(principal.getUsername()));
    }
}
