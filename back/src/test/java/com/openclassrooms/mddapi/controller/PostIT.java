package com.openclassrooms.mddapi.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.openclassrooms.mddapi.model.User;
import com.openclassrooms.mddapi.model.Post;
import com.openclassrooms.mddapi.support.BaseIntegrationTest;
import com.openclassrooms.mddapi.model.Subscription;
import com.openclassrooms.mddapi.model.Topic;

class PostIT extends BaseIntegrationTest {

    private static final String EMAIL = "poster@example.com";
    private static final String USERNAME = "poster";
    private static final String TOPIC_TITLE = "Angular";
    private static final String TOPIC_DESCRIPTION = "TypeScript front-end framework.";
    private static final String POST_TITLE = "First post";
    private static final String POST_CONTENT = "Content of the first post.";
    private static final Instant POST_DATE = Instant.parse("2026-01-15T10:00:00Z");

    @Test
    void postsRoutes_whenNoToken_returnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Unauthorized"));

        mockMvc.perform(get("/api/posts/%d".formatted(UNKNOWN_ID)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(POST_TITLE, POST_CONTENT, UNKNOWN_ID)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postsRoutes_whenInvalidToken_returnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/posts")
                        .header(HttpHeaders.AUTHORIZATION, bearer("not.a.token")))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/posts/%d".formatted(UNKNOWN_ID))
                        .header(HttpHeaders.AUTHORIZATION, bearer("not.a.token")))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/posts")
                        .header(HttpHeaders.AUTHORIZATION, bearer("not.a.token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(POST_TITLE, POST_CONTENT, UNKNOWN_ID)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_whenValid_returnsCreatedAndPersistsPost() throws Exception {
        User user = userRepository.save(newUser(EMAIL, USERNAME, PASSWORD));
        Topic topic = persistedTopic(TOPIC_TITLE, TOPIC_DESCRIPTION);
        String token = loginToken(EMAIL, PASSWORD);

        mockMvc.perform(post("/api/posts")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(POST_TITLE, POST_CONTENT, topic.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(POST_TITLE))
                .andExpect(jsonPath("$.content").value(POST_CONTENT))
                .andExpect(jsonPath("$.author").value(USERNAME))
                .andExpect(jsonPath("$.topicId").value(topic.getId()))
                .andExpect(jsonPath("$.date").isNotEmpty());

        assertThat(postRepository.findAll()).singleElement().satisfies(post -> {
            assertThat(post.getUserId()).isEqualTo(user.getId());
            assertThat(post.getTopicId()).isEqualTo(topic.getId());
            assertThat(post.getTitle()).isEqualTo(POST_TITLE);
        });
    }

    @Test
    void create_whenUnknownTopic_returnsNotFoundAndDoesNotPersist() throws Exception {
        userRepository.save(newUser(EMAIL, USERNAME, PASSWORD));
        String token = loginToken(EMAIL, PASSWORD);

        mockMvc.perform(post("/api/posts")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(POST_TITLE, POST_CONTENT, UNKNOWN_ID)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Topic not found"));

        assertThat(postRepository.findAll()).isEmpty();
    }

    @Test
    void create_whenInvalidBody_returnsBadRequest() throws Exception {
        userRepository.save(newUser(EMAIL, USERNAME, PASSWORD));
        String token = loginToken(EMAIL, PASSWORD);

        mockMvc.perform(post("/api/posts")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody("", POST_CONTENT, UNKNOWN_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void list_whenSubscribed_returnsOnlyPostsOfSubscribedTopics() throws Exception {
        User user = userRepository.save(newUser(EMAIL, USERNAME, PASSWORD));
        Topic subscribedTopic = persistedTopic(TOPIC_TITLE, TOPIC_DESCRIPTION);
        Topic otherTopic = persistedTopic("Spring Boot", "Java framework.");
        subscriptionRepository.save(new Subscription(user.getId(), subscribedTopic.getId()));
        persistedPost(POST_TITLE, POST_CONTENT, POST_DATE, subscribedTopic.getId(), user.getId());
        persistedPost("Hidden", "Should not appear.", POST_DATE.plusSeconds(60), otherTopic.getId(), user.getId());
        String token = loginToken(EMAIL, PASSWORD);

        mockMvc.perform(get("/api/posts")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value(POST_TITLE))
                .andExpect(jsonPath("$[0].author").value(USERNAME));
    }

    @Test
    void list_whenNoSubscription_returnsEmpty() throws Exception {
        userRepository.save(newUser(EMAIL, USERNAME, PASSWORD));
        Topic topic = persistedTopic(TOPIC_TITLE, TOPIC_DESCRIPTION);
        persistedPost(POST_TITLE, POST_CONTENT, POST_DATE, topic.getId(), 1L);
        String token = loginToken(EMAIL, PASSWORD);

        mockMvc.perform(get("/api/posts")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void list_whenOrderAsc_returnsOldestFirst() throws Exception {
        User user = userRepository.save(newUser(EMAIL, USERNAME, PASSWORD));
        Topic topic = persistedTopic(TOPIC_TITLE, TOPIC_DESCRIPTION);
        subscriptionRepository.save(new Subscription(user.getId(), topic.getId()));
        persistedPost("Recent", "Most recent post.", POST_DATE.plusSeconds(120), topic.getId(), user.getId());
        persistedPost("Oldest", "Oldest post.", POST_DATE, topic.getId(), user.getId());
        String token = loginToken(EMAIL, PASSWORD);

        mockMvc.perform(get("/api/posts").param("order", "asc")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Oldest"))
                .andExpect(jsonPath("$[1].title").value("Recent"));
    }

    @Test
    void list_whenOrderDescByDefault_returnsNewestFirst() throws Exception {
        User user = userRepository.save(newUser(EMAIL, USERNAME, PASSWORD));
        Topic topic = persistedTopic(TOPIC_TITLE, TOPIC_DESCRIPTION);
        subscriptionRepository.save(new Subscription(user.getId(), topic.getId()));
        persistedPost("Recent", "Most recent post.", POST_DATE.plusSeconds(120), topic.getId(), user.getId());
        persistedPost("Oldest", "Oldest post.", POST_DATE, topic.getId(), user.getId());
        String token = loginToken(EMAIL, PASSWORD);

        mockMvc.perform(get("/api/posts")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Recent"))
                .andExpect(jsonPath("$[1].title").value("Oldest"));
    }

    @Test
    void list_whenInvalidOrder_returnsBadRequest() throws Exception {
        userRepository.save(newUser(EMAIL, USERNAME, PASSWORD));
        String token = loginToken(EMAIL, PASSWORD);

        mockMvc.perform(get("/api/posts").param("order", "sideways")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void detail_whenPostExists_returnsPostEvenIfTopicNotSubscribed() throws Exception {
        User user = userRepository.save(newUser(EMAIL, USERNAME, PASSWORD));
        Topic topic = persistedTopic(TOPIC_TITLE, TOPIC_DESCRIPTION);
        Post post = persistedPost(POST_TITLE, POST_CONTENT, POST_DATE, topic.getId(), user.getId());
        String token = loginToken(EMAIL, PASSWORD);

        mockMvc.perform(get("/api/posts/%d".formatted(post.getId()))
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(POST_TITLE))
                .andExpect(jsonPath("$.author").value(USERNAME))
                .andExpect(content().string(not(containsString("password"))));
    }

    @Test
    void detail_whenPostMissing_returnsNotFound() throws Exception {
        userRepository.save(newUser(EMAIL, USERNAME, PASSWORD));
        String token = loginToken(EMAIL, PASSWORD);

        mockMvc.perform(get("/api/posts/%d".formatted(UNKNOWN_ID))
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Post not found"));
    }

    private String createBody(String title, String content, Long topicId) {
        return """
                {"title":"%s","content":"%s","topicId":%d}
                """.formatted(title, content, topicId);
    }
}
