package com.openclassrooms.mddapi.authentification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.openclassrooms.mddapi.authentification.dto.AuthResponse;
import com.openclassrooms.mddapi.authentification.dto.LoginRequest;
import com.openclassrooms.mddapi.authentification.dto.RegisterRequest;
import com.openclassrooms.mddapi.authentification.exception.EmailAlreadyUsedException;
import com.openclassrooms.mddapi.authentification.exception.UsernameAlreadyUsedException;
import com.openclassrooms.mddapi.authentification.model.User;
import com.openclassrooms.mddapi.authentification.repository.UserRepository;
import com.openclassrooms.mddapi.authentification.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final long USER_ID = 1L;
    private static final String EMAIL = "user@example.com";
    private static final String USERNAME = "testuser";
    private static final String RAW_PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "$2a$10$encoded";
    private static final String TOKEN = "jwt-token";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, authenticationManager, jwtService);
    }

    @Test
    void register_whenEmailAlreadyUsed_throwsAndDoesNotSave() {
        RegisterRequest request = new RegisterRequest(EMAIL, USERNAME, RAW_PASSWORD);
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyUsedException.class);
        verify(userRepository, never()).save(any());
        verifyNoInteractions(jwtService);
    }

    @Test
    void register_whenUsernameAlreadyUsed_throwsAndDoesNotSave() {
        RegisterRequest request = new RegisterRequest(EMAIL, USERNAME, RAW_PASSWORD);
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userRepository.existsByUsername(USERNAME)).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UsernameAlreadyUsedException.class);
        verify(userRepository, never()).save(any());
        verifyNoInteractions(jwtService);
    }

    @Test
    void register_whenValid_encodesPasswordAndReturnsToken() {
        RegisterRequest request = new RegisterRequest(EMAIL, USERNAME, RAW_PASSWORD);
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(new User(USER_ID, EMAIL, USERNAME, ENCODED_PASSWORD));
        when(jwtService.generateToken(EMAIL, USER_ID, USERNAME)).thenReturn(TOKEN);

        AuthResponse response = authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo(ENCODED_PASSWORD);
        assertThat(response.token()).isEqualTo(TOKEN);
    }

    @Test
    void login_whenValidCredentials_returnsToken() {
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(new User(USER_ID, EMAIL, USERNAME, ENCODED_PASSWORD)));
        when(jwtService.generateToken(EMAIL, USER_ID, USERNAME)).thenReturn(TOKEN);

        AuthResponse response = authService.login(new LoginRequest(EMAIL, RAW_PASSWORD));

        assertThat(response.token()).isEqualTo(TOKEN);
    }

    @Test
    void login_whenBadCredentials_propagatesException() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> authService.login(new LoginRequest(EMAIL, RAW_PASSWORD)))
                .isInstanceOf(BadCredentialsException.class);
        verifyNoInteractions(jwtService);
    }
}
