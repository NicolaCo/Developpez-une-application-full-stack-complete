package com.openclassrooms.mddapi.authentication.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import jakarta.servlet.FilterChain;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String SUBJECT = "user@example.com";

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthUserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_whenNoAuthorizationHeader_continuesWithoutAuthentication() throws Exception {
        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }

    @Test
    void doFilter_whenHeaderWithoutBearerPrefix_continuesWithoutAuthentication() throws Exception {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic abc");

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }

    @Test
    void doFilter_whenTokenValid_authenticatesUser() throws Exception {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer valid.jwt");
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(SUBJECT, "pwd", List.of());
        when(jwtService.extractValidSubject("valid.jwt")).thenReturn(Optional.of(SUBJECT));
        when(userDetailsService.loadUserByUsername(SUBJECT)).thenReturn(userDetails);

        filter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo(SUBJECT);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_whenTokenInvalid_continuesWithoutAuthentication() throws Exception {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer invalid.jwt");
        when(jwtService.extractValidSubject("invalid.jwt")).thenReturn(Optional.empty());

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_whenUserNoLongerExists_continuesWithoutAuthentication() throws Exception {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer deleted.jwt");
        when(jwtService.extractValidSubject("deleted.jwt")).thenReturn(Optional.of(SUBJECT));
        when(userDetailsService.loadUserByUsername(SUBJECT)).thenThrow(new UsernameNotFoundException(SUBJECT));

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}
