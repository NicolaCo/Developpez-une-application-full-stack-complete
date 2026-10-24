package com.openclassrooms.mddapi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.jayway.jsonpath.JsonPath;
import com.openclassrooms.mddapi.model.User;
import com.openclassrooms.mddapi.support.BaseIntegrationTest;

class AuthIT extends BaseIntegrationTest {

    private static final String EMAIL = "user@example.com";
    private static final String USERNAME = "testuser";
    private static final String UNKNOWN_EMAIL = "unknown@example.com";

    @Test
    void register_whenValid_createsUserWithEncodedPassword() throws Exception {
        String body = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody(EMAIL, USERNAME, PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").doesNotExist())
                .andExpect(jsonPath("$.id").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        User saved = userRepository.findByEmail(EMAIL).orElseThrow();
        assertThat(saved.getUsername()).isEqualTo(USERNAME);
        assertThat(saved.getPassword()).isNotEqualTo(PASSWORD);
        assertThat(passwordEncoder.matches(PASSWORD, saved.getPassword())).isTrue();

        String token = JsonPath.read(body, "$.token");
        assertThat(jwtService.extractValidSubject(token)).contains(EMAIL);
    }

    @Test
    void register_whenEmailAlreadyUsed_returnsConflict() throws Exception {
        userRepository.save(newUser(EMAIL, USERNAME, PASSWORD));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody(EMAIL, "otheruser", PASSWORD)))
                .andExpect(status().isConflict());
    }

    @Test
    void register_whenUsernameAlreadyUsed_returnsConflict() throws Exception {
        userRepository.save(newUser(EMAIL, USERNAME, PASSWORD));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("other@example.com", USERNAME, PASSWORD)))
                .andExpect(status().isConflict());
    }

    @Test
    void register_whenInvalidPayload_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("not-an-email", USERNAME, PASSWORD)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").doesNotExist())
                .andExpect(jsonPath("$.timestamp").doesNotExist());
    }

    @Test
    void login_whenValidCredentials_returnsToken() throws Exception {
        userRepository.save(newUser(EMAIL, USERNAME, PASSWORD));

        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(EMAIL, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").doesNotExist())
                .andExpect(jsonPath("$.id").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        String token = JsonPath.read(body, "$.token");
        assertThat(jwtService.extractValidSubject(token)).contains(EMAIL);
    }

    @Test
    void login_whenWrongPassword_returnsUnauthorized() throws Exception {
        userRepository.save(newUser(EMAIL, USERNAME, PASSWORD));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(EMAIL, "wrongpassword")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void protectedRoute_whenNoToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/topics"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    @Test
    void protectedRoute_whenInvalidToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/topics")
                        .header(HttpHeaders.AUTHORIZATION, bearer("not.a.token")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    @Test
    void protectedRoute_whenValidToken_passesAuthentication() throws Exception {
        userRepository.save(newUser(EMAIL, USERNAME, PASSWORD));
        String token = loginToken(EMAIL, PASSWORD);

        mockMvc.perform(get("/api/topics")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void publicAuthRoutes_areReachableWithoutToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(UNKNOWN_EMAIL, PASSWORD)))
                .andExpect(status().isUnauthorized());
    }
}
