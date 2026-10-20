package com.openclassrooms.mddapi.support;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.mysql.MySQLContainer;

import com.jayway.jsonpath.JsonPath;
import com.openclassrooms.mddapi.authentication.model.User;
import com.openclassrooms.mddapi.authentication.repository.UserRepository;
import com.openclassrooms.mddapi.authentication.security.JwtService;
import com.openclassrooms.mddapi.topic.model.Topic;
import com.openclassrooms.mddapi.topic.repository.SubscriptionRepository;
import com.openclassrooms.mddapi.topic.repository.TopicRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(BaseIntegrationTest.MySqlContainerConfiguration.class)
public abstract class BaseIntegrationTest {

    protected static final String PASSWORD = "password123";
    protected static final long UNKNOWN_ID = 9999L;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected TopicRepository topicRepository;

    @Autowired
    protected SubscriptionRepository subscriptionRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected JwtService jwtService;

    @TestConfiguration(proxyBeanMethods = false)
    static class MySqlContainerConfiguration {

        @Bean
        @ServiceConnection
        MySQLContainer mysqlContainer() {
            return new MySQLContainer("mysql:8.0");
        }
    }

    @BeforeEach
    void cleanDatabase() {
        subscriptionRepository.deleteAll();
        topicRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }

    protected String loginToken(String email, String password) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(email, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    protected String registerBody(String email, String username, String password) {
        return """
                {"email":"%s","username":"%s","password":"%s"}
                """.formatted(email, username, password);
    }

    protected String loginBody(String email, String password) {
        return """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);
    }

    protected User newUser(String email, String username, String rawPassword) {
        return new User(email, username, passwordEncoder.encode(rawPassword));
    }

    protected Topic persistedTopic(String title, String description) {
        return topicRepository.save(new Topic(title, description));
    }
}
