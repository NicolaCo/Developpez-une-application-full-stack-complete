package com.openclassrooms.mddapi.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class SecurityCurrentUserProviderTest {

    private static final long USER_ID = 7L;
    private static final String EMAIL = "user@example.com";
    private static final String ENCODED_PASSWORD = "$2a$10$encoded";

    private SecurityCurrentUserProvider provider;

    @BeforeEach
    void setUp() {
        provider = new SecurityCurrentUserProvider();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void currentUserId_whenPrincipalIsAuthUser_returnsId() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new AuthUser(USER_ID, EMAIL, ENCODED_PASSWORD), null, List.of()));

        assertThat(provider.currentUserId()).isEqualTo(USER_ID);
    }

    @Test
    void currentUserId_whenNoAuthentication_throwsIllegalState() {
        assertThatThrownBy(() -> provider.currentUserId())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No AuthUser in security context");
    }

    @Test
    void currentUserId_whenPrincipalIsNotAuthUser_throwsIllegalState() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(EMAIL, null, List.of()));

        assertThatThrownBy(() -> provider.currentUserId())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No AuthUser in security context");
    }
}
