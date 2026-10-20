package com.openclassrooms.mddapi.common;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.openclassrooms.mddapi.authentication.model.User;
import com.openclassrooms.mddapi.support.BaseIntegrationTest;

class ErrorContractIT extends BaseIntegrationTest {

    private static final String REGISTER_PATH = "/api/auth/register";
    private static final String UNKNOWN_PATH = "/api/auth/unknown";
    private static final String SUBSCRIBE_PATH = "/api/topics/abc/subscribe";

    @Test
    void post_whenMalformedJson_returns400ApiError() throws Exception {
        mockMvc.perform(post(REGISTER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed request body"));
    }

    @Test
    void get_whenMethodNotSupported_returns405ApiErrorWithAllowHeader() throws Exception {
        mockMvc.perform(get(REGISTER_PATH))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string(HttpHeaders.ALLOW, "POST"))
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.message").value("Method not allowed"));
    }

    @Test
    void post_whenUnsupportedMediaType_returns415ApiErrorWithAcceptHeader() throws Exception {
        mockMvc.perform(post(REGISTER_PATH)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("not json"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(header().string(HttpHeaders.ACCEPT, containsString("application/json")))
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.message").value("Unsupported media type"));
    }

    @Test
    void get_whenRouteUnknown_returns404ApiError() throws Exception {
        mockMvc.perform(get(UNKNOWN_PATH))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Resource not found"));
    }

    @Test
    void subscribe_whenTopicIdNotNumeric_returns400ApiError() throws Exception {
        User user = userRepository.save(newUser("numeric@example.com", "numericuser", "password123"));
        String token = loginToken(user.getEmail(), "password123");

        mockMvc.perform(post(SUBSCRIBE_PATH).header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid request parameter"));
    }
}
