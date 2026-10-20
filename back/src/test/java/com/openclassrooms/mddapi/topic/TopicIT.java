package com.openclassrooms.mddapi.topic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import com.openclassrooms.mddapi.topic.model.Subscription;
import com.openclassrooms.mddapi.topic.model.Topic;

class TopicIT extends BaseIntegrationTest {

    private static final String EMAIL = "subscriber@example.com";
    private static final String USERNAME = "subscriber";
    private static final String TOPIC_ONE_TITLE = "Angular";
    private static final String TOPIC_ONE_DESCRIPTION = "TypeScript front-end framework for building modern web applications.";
    private static final String TOPIC_TWO_TITLE = "Spring Boot";
    private static final String TOPIC_TWO_DESCRIPTION = "Java framework for building REST APIs and server applications.";

    @Test
    void topicsRoutes_whenNoToken_returnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/topics"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Unauthorized"));

        mockMvc.perform(post("/api/topics/%d/subscribe".formatted(UNKNOWN_ID)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/topics/%d/subscribe".formatted(UNKNOWN_ID)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void topicsRoutes_whenInvalidToken_returnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/topics")
                        .header(HttpHeaders.AUTHORIZATION, bearer("not.a.token")))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/topics/%d/subscribe".formatted(UNKNOWN_ID))
                        .header(HttpHeaders.AUTHORIZATION, bearer("not.a.token")))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/topics/%d/subscribe".formatted(UNKNOWN_ID))
                        .header(HttpHeaders.AUTHORIZATION, bearer("not.a.token")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void list_whenAuthenticated_returnsTopicsOrderedWithSubscriptionFlag() throws Exception {
        User user = userRepository.save(newUser(EMAIL, USERNAME, PASSWORD));
        Topic topicOne = persistedTopic(TOPIC_ONE_TITLE, TOPIC_ONE_DESCRIPTION);
        Topic topicTwo = persistedTopic(TOPIC_TWO_TITLE, TOPIC_TWO_DESCRIPTION);
        subscriptionRepository.save(new Subscription(user.getId(), topicOne.getId()));
        String token = loginToken(EMAIL, PASSWORD);

        mockMvc.perform(get("/api/topics")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value(TOPIC_ONE_TITLE))
                .andExpect(jsonPath("$[0].description").value(TOPIC_ONE_DESCRIPTION))
                .andExpect(jsonPath("$[0].isSubscribed").value(true))
                .andExpect(jsonPath("$[1].title").value(TOPIC_TWO_TITLE))
                .andExpect(jsonPath("$[1].description").value(TOPIC_TWO_DESCRIPTION))
                .andExpect(jsonPath("$[1].isSubscribed").value(false));
    }

    @Test
    void subscribe_whenNotSubscribed_returnsCreatedAndPersistsSubscription() throws Exception {
        User user = userRepository.save(newUser(EMAIL, USERNAME, PASSWORD));
        Topic topic = persistedTopic(TOPIC_ONE_TITLE, TOPIC_ONE_DESCRIPTION);
        String token = loginToken(EMAIL, PASSWORD);

        mockMvc.perform(post("/api/topics/%d/subscribe".formatted(topic.getId()))
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        assertThat(subscriptionRepository.findByUserIdAndTopicId(user.getId(), topic.getId())).isPresent();
    }

    @Test
    void subscribe_whenAlreadySubscribed_returnsConflict() throws Exception {
        User user = userRepository.save(newUser(EMAIL, USERNAME, PASSWORD));
        Topic topic = persistedTopic(TOPIC_ONE_TITLE, TOPIC_ONE_DESCRIPTION);
        subscriptionRepository.save(new Subscription(user.getId(), topic.getId()));
        String token = loginToken(EMAIL, PASSWORD);

        mockMvc.perform(post("/api/topics/%d/subscribe".formatted(topic.getId()))
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Already subscribed to this topic"));

        assertThat(subscriptionRepository.findAllByUserId(user.getId())).hasSize(1);
    }

    @Test
    void subscribe_whenUnknownTopic_returnsNotFound() throws Exception {
        userRepository.save(newUser(EMAIL, USERNAME, PASSWORD));
        String token = loginToken(EMAIL, PASSWORD);

        mockMvc.perform(post("/api/topics/%d/subscribe".formatted(UNKNOWN_ID))
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Topic not found: " + UNKNOWN_ID));

        assertThat(subscriptionRepository.findAll()).isEmpty();
    }

    @Test
    void unsubscribe_whenSubscribed_returnsNoContentAndDeletesSubscription() throws Exception {
        User user = userRepository.save(newUser(EMAIL, USERNAME, PASSWORD));
        Topic topic = persistedTopic(TOPIC_ONE_TITLE, TOPIC_ONE_DESCRIPTION);
        subscriptionRepository.save(new Subscription(user.getId(), topic.getId()));
        String token = loginToken(EMAIL, PASSWORD);

        mockMvc.perform(delete("/api/topics/%d/subscribe".formatted(topic.getId()))
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNoContent());

        assertThat(subscriptionRepository.findAllByUserId(user.getId())).isEmpty();
    }

    @Test
    void unsubscribe_whenNotSubscribed_returnsNoContent() throws Exception {
        User user = userRepository.save(newUser(EMAIL, USERNAME, PASSWORD));
        Topic topic = persistedTopic(TOPIC_ONE_TITLE, TOPIC_ONE_DESCRIPTION);
        String token = loginToken(EMAIL, PASSWORD);

        mockMvc.perform(delete("/api/topics/%d/subscribe".formatted(topic.getId()))
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNoContent());

        assertThat(subscriptionRepository.findAllByUserId(user.getId())).isEmpty();
    }

    @Test
    void unsubscribe_whenUnknownTopic_returnsNotFound() throws Exception {
        userRepository.save(newUser(EMAIL, USERNAME, PASSWORD));
        String token = loginToken(EMAIL, PASSWORD);

        mockMvc.perform(delete("/api/topics/%d/subscribe".formatted(UNKNOWN_ID))
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Topic not found: " + UNKNOWN_ID));
    }
}
