package com.openclassrooms.mddapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import com.openclassrooms.mddapi.security.SecurityCurrentUserProvider;
import com.openclassrooms.mddapi.dto.post.CreatePostRequest;
import com.openclassrooms.mddapi.dto.post.PostResponse;
import com.openclassrooms.mddapi.exception.PostNotFoundException;
import com.openclassrooms.mddapi.exception.TopicNotFoundException;
import com.openclassrooms.mddapi.model.Post;
import com.openclassrooms.mddapi.repository.PostRepository;
import com.openclassrooms.mddapi.model.Subscription;
import com.openclassrooms.mddapi.repository.SubscriptionRepository;
import com.openclassrooms.mddapi.repository.TopicRepository;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    private static final long USER_ID = 1L;
    private static final long SUBSCRIBED_TOPIC_ID = 10L;
    private static final long OTHER_TOPIC_ID = 20L;
    private static final long UNKNOWN_TOPIC_ID = 9999L;
    private static final long UNKNOWN_POST_ID = 8888L;
    private static final Instant POST_DATE = Instant.parse("2026-01-15T10:00:00Z");
    private static final String AUTHOR = "authorOne";
    private static final String POST_TITLE = "First post";
    private static final String POST_CONTENT = "Content of the first post.";

    @Mock
    private PostRepository postRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SecurityCurrentUserProvider currentUserProvider;

    @Mock
    private UserDisplay userDisplay;

    private PostService postService;

    @BeforeEach
    void setUp() {
        postService = new PostService(postRepository, topicRepository, subscriptionRepository,
                currentUserProvider, userDisplay);
    }

    @Test
    void create_whenTopicMissing_throwsAndDoesNotSave() {
        when(topicRepository.existsById(UNKNOWN_TOPIC_ID)).thenReturn(false);

        assertThatThrownBy(() -> postService.create(new CreatePostRequest(POST_TITLE, POST_CONTENT, UNKNOWN_TOPIC_ID)))
                .isInstanceOf(TopicNotFoundException.class);
        verify(postRepository, never()).save(any());
    }

    @Test
    void create_whenValid_persistsPostForCurrentUserWithCurrentDate() {
        when(topicRepository.existsById(SUBSCRIBED_TOPIC_ID)).thenReturn(true);
        when(currentUserProvider.currentUserId()).thenReturn(USER_ID);
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userDisplay.usernamesOf(Set.of(USER_ID))).thenReturn(Map.of(USER_ID, AUTHOR));

        PostResponse response = postService.create(
                new CreatePostRequest(POST_TITLE, POST_CONTENT, SUBSCRIBED_TOPIC_ID));

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(USER_ID);
        assertThat(captor.getValue().getTopicId()).isEqualTo(SUBSCRIBED_TOPIC_ID);
        assertThat(captor.getValue().getDate()).isNotNull();
        assertThat(response.author()).isEqualTo(AUTHOR);
        assertThat(response.title()).isEqualTo(POST_TITLE);
    }

    @Test
    void list_whenNoSubscription_returnsEmptyWithoutQueryingPosts() {
        when(currentUserProvider.currentUserId()).thenReturn(USER_ID);
        when(subscriptionRepository.findAllByUserId(USER_ID)).thenReturn(List.of());

        assertThat(postService.list(Sort.Direction.DESC)).isEmpty();
        verify(postRepository, never()).findAllByTopicIdIn(any(), any());
    }

    @Test
    void list_whenSubscribed_returnsPostsOfSubscribedTopicsWithAuthor() {
        when(currentUserProvider.currentUserId()).thenReturn(USER_ID);
        when(subscriptionRepository.findAllByUserId(USER_ID))
                .thenReturn(List.of(new Subscription(USER_ID, SUBSCRIBED_TOPIC_ID)));
        when(postRepository.findAllByTopicIdIn(eq(Set.of(SUBSCRIBED_TOPIC_ID)), eq(Sort.by(Sort.Direction.DESC, "date"))))
                .thenReturn(List.of(new Post(1L, POST_TITLE, POST_CONTENT, POST_DATE, SUBSCRIBED_TOPIC_ID, USER_ID)));
        when(userDisplay.usernamesOf(Set.of(USER_ID))).thenReturn(Map.of(USER_ID, AUTHOR));

        List<PostResponse> posts = postService.list(Sort.Direction.DESC);

        assertThat(posts).containsExactly(
                new PostResponse(1L, POST_TITLE, POST_CONTENT, POST_DATE, AUTHOR, SUBSCRIBED_TOPIC_ID));
    }

    @Test
    void detail_whenPostMissing_throws() {
        when(postRepository.findById(UNKNOWN_POST_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.detail(UNKNOWN_POST_ID))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    void detail_whenPostExists_returnsPostWithAuthor() {
        when(postRepository.findById(1L))
                .thenReturn(Optional.of(new Post(1L, POST_TITLE, POST_CONTENT, POST_DATE, OTHER_TOPIC_ID, USER_ID)));
        when(userDisplay.usernamesOf(Set.of(USER_ID))).thenReturn(Map.of(USER_ID, AUTHOR));

        PostResponse response = postService.detail(1L);

        assertThat(response.author()).isEqualTo(AUTHOR);
        assertThat(response.topicId()).isEqualTo(OTHER_TOPIC_ID);
    }
}
