package com.openclassrooms.mddapi.authentification.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.openclassrooms.mddapi.authentification.dto.AuthResponse;
import com.openclassrooms.mddapi.authentification.dto.LoginRequest;
import com.openclassrooms.mddapi.authentification.dto.RegisterRequest;
import com.openclassrooms.mddapi.authentification.exception.EmailAlreadyUsedException;
import com.openclassrooms.mddapi.authentification.exception.UsernameAlreadyUsedException;
import com.openclassrooms.mddapi.authentification.model.User;
import com.openclassrooms.mddapi.authentification.repository.UserRepository;
import com.openclassrooms.mddapi.authentification.security.JwtService;

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
        String token = jwtService.generateToken(saved.getEmail(), saved.getId(), saved.getUsername());
        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + request.email()));
        String token = jwtService.generateToken(user.getEmail(), user.getId(), user.getUsername());
        return new AuthResponse(token);
    }
}
