package com.openclassrooms.mddapi.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
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
import com.openclassrooms.mddapi.model.Topic;

class CommentIT extends BaseIntegrationTest {

    private static final String EMAIL = "commenter@example.com";
    private static final String USERNAME = "commenter";
    private static final String TOPIC_TITLE = "Angular";
    private static final String TOPIC_DESCRIPTION = "TypeScript front-end framework.";
    private static final String POST_TITLE = "First post";
    private static final String POST_CONTENT = "Content of the first post.";
    private static final String COMMENT_CONTENT = "First comment";
    private static final Instant POST_DATE = Instant.parse("2026-01-15T10:00:00Z");

    @Test
    void commentsRoutes_whenNoToken_returnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/posts/%d/comments".formatted(UNKNOWN_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        mockMvc.perform(post("/api/posts/%d/comments".formatted(UNKNOWN_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentBody(COMMENT_CONTENT)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void commentsRoutes_whenInvalidToken_returnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/posts/%d/comments".formatted(UNKNOWN_ID))
                        .header(HttpHeaders.AUTHORIZATION, bearer("not.a.token")))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/posts/%d/comments".formatted(UNKNOWN_ID))
                        .header(HttpHeaders.AUTHORIZATION, bearer("not.a.token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentBody(COMMENT_CONTENT)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_whenValid_returnsCreatedAndPersistsComment() throws Exception {
        User user = userRepository.save(newUser(EMAIL, USERNAME, PASSWORD));
        Topic topic = persistedTopic(TOPIC_TITLE, TOPIC_DESCRIPTION);
        Post post = persistedPost(POST_TITLE, POST_CONTENT, POST_DATE, topic.getId(), user.getId());
        String token = loginToken(EMAIL, PASSWORD);

        mockMvc.perform(post("/api/posts/%d/comments".formatted(post.getId()))
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentBody(COMMENT_CONTENT)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value(COMMENT_CONTENT))
                .andExpect(jsonPath("$.author").value(USERNAME))
                .andExpect(jsonPath("$.postId").value(post.getId()))
                .andExpect(jsonPath("$.date").isNotEmpty());

        assertThat(commentRepository.findAll()).singleElement().satisfies(comment -> {
            assertThat(comment.getUserId()).isEqualTo(user.getId());
            assertThat(comment.getPostId()).isEqualTo(post.getId());
            assertThat(comment.getContent()).isEqualTo(COMMENT_CONTENT);
        });
    }

    @Test
    void create_whenPostMissing_returnsNotFoundAndDoesNotPersist() throws Exception {
        userRepository.save(newUser(EMAIL, USERNAME, PASSWORD));
        String token = loginToken(EMAIL, PASSWORD);

        mockMvc.perform(post("/api/posts/%d/comments".formatted(UNKNOWN_ID))
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentBody(COMMENT_CONTENT)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Post not found"));

        assertThat(commentRepository.findAll()).isEmpty();
    }

    @Test
    void create_whenInvalidBody_returnsBadRequest() throws Exception {
        User user = userRepository.save(newUser(EMAIL, USERNAME, PASSWORD));
        Topic topic = persistedTopic(TOPIC_TITLE, TOPIC_DESCRIPTION);
        Post post = persistedPost(POST_TITLE, POST_CONTENT, POST_DATE, topic.getId(), user.getId());
        String token = loginToken(EMAIL, PASSWORD);

        mockMvc.perform(post("/api/posts/%d/comments".formatted(post.getId()))
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentBody("")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void list_whenPostExists_returnsCommentsOrderedByDateAsc() throws Exception {
        User user = userRepository.save(newUser(EMAIL, USERNAME, PASSWORD));
        Topic topic = persistedTopic(TOPIC_TITLE, TOPIC_DESCRIPTION);
        Post post = persistedPost(POST_TITLE, POST_CONTENT, POST_DATE, topic.getId(), user.getId());
        persistedComment("Recent", POST_DATE.plusSeconds(120), post.getId(), user.getId());
        persistedComment("Oldest", POST_DATE, post.getId(), user.getId());
        String token = loginToken(EMAIL, PASSWORD);

        mockMvc.perform(get("/api/posts/%d/comments".formatted(post.getId()))
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].content").value("Oldest"))
                .andExpect(jsonPath("$[1].content").value("Recent"))
                .andExpect(jsonPath("$[0].author").value(USERNAME))
                .andExpect(content().string(not(containsString("password"))));
    }

    @Test
    void list_whenPostMissing_returnsNotFound() throws Exception {
        userRepository.save(newUser(EMAIL, USERNAME, PASSWORD));
        String token = loginToken(EMAIL, PASSWORD);

        mockMvc.perform(get("/api/posts/%d/comments".formatted(UNKNOWN_ID))
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Post not found"));
    }

    private String commentBody(String content) {
        return """
                {"content":"%s"}
                """.formatted(content);
    }
}
