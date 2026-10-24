package com.openclassrooms.mddapi.config;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import com.openclassrooms.mddapi.support.BaseIntegrationTest;

class CorsIT extends BaseIntegrationTest {

    private static final String FRONT_ORIGIN = "http://localhost:4200";
    private static final String OTHER_ORIGIN = "http://evil.example";
    private static final String TOPICS_PATH = "/api/topics";

    @Test
    void options_whenPreflightFromFrontOrigin_returnsAllowedCorsHeaders() throws Exception {
        mockMvc.perform(options(TOPICS_PATH)
                        .header(HttpHeaders.ORIGIN, FRONT_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, FRONT_ORIGIN))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, containsString("GET")))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, containsString("authorization")));
    }

    @Test
    void get_whenCrossOriginWithoutToken_stillAdvertisesAllowedOrigin() throws Exception {
        mockMvc.perform(get(TOPICS_PATH).header(HttpHeaders.ORIGIN, FRONT_ORIGIN))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, FRONT_ORIGIN));
    }

    @Test
    void get_whenOriginNotAllowed_doesNotAdvertiseCorsHeaders() throws Exception {
        mockMvc.perform(get(TOPICS_PATH).header(HttpHeaders.ORIGIN, OTHER_ORIGIN))
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }
}
