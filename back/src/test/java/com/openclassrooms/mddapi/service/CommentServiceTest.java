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
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import com.openclassrooms.mddapi.security.SecurityCurrentUserProvider;
import com.openclassrooms.mddapi.dto.post.CommentResponse;
import com.openclassrooms.mddapi.dto.post.CreateCommentRequest;
import com.openclassrooms.mddapi.exception.PostNotFoundException;
import com.openclassrooms.mddapi.model.Comment;
import com.openclassrooms.mddapi.repository.CommentRepository;
import com.openclassrooms.mddapi.repository.PostRepository;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    private static final long USER_ID = 1L;
    private static final long POST_ID = 10L;
    private static final long UNKNOWN_POST_ID = 9999L;
    private static final Instant COMMENT_DATE = Instant.parse("2026-01-15T10:00:00Z");
    private static final String AUTHOR = "authorOne";
    private static final String COMMENT_CONTENT = "First comment";

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private SecurityCurrentUserProvider currentUserProvider;

    @Mock
    private UserDisplay userDisplay;

    private CommentService commentService;

    @BeforeEach
    void setUp() {
        commentService = new CommentService(commentRepository, postRepository, currentUserProvider, userDisplay);
    }

    @Test
    void create_whenPostMissing_throwsAndDoesNotSave() {
        when(postRepository.existsById(UNKNOWN_POST_ID)).thenReturn(false);

        assertThatThrownBy(() -> commentService.create(UNKNOWN_POST_ID, new CreateCommentRequest(COMMENT_CONTENT)))
                .isInstanceOf(PostNotFoundException.class);
        verify(commentRepository, never()).save(any());
    }

    @Test
    void create_whenValid_persistsCommentForCurrentUserWithCurrentDate() {
        when(postRepository.existsById(POST_ID)).thenReturn(true);
        when(currentUserProvider.currentUserId()).thenReturn(USER_ID);
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userDisplay.usernamesOf(Set.of(USER_ID))).thenReturn(Map.of(USER_ID, AUTHOR));

        CommentResponse response = commentService.create(POST_ID, new CreateCommentRequest(COMMENT_CONTENT));

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(USER_ID);
        assertThat(captor.getValue().getPostId()).isEqualTo(POST_ID);
        assertThat(captor.getValue().getDate()).isNotNull();
        assertThat(response.author()).isEqualTo(AUTHOR);
        assertThat(response.content()).isEqualTo(COMMENT_CONTENT);
    }

    @Test
    void list_whenPostMissing_throws() {
        when(postRepository.existsById(UNKNOWN_POST_ID)).thenReturn(false);

        assertThatThrownBy(() -> commentService.list(UNKNOWN_POST_ID))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    void list_whenPostExists_returnsCommentsOrderedByDateAscWithAuthor() {
        when(postRepository.existsById(POST_ID)).thenReturn(true);
        when(commentRepository.findAllByPostId(eq(POST_ID), eq(Sort.by(Sort.Direction.ASC, "date"))))
                .thenReturn(List.of(new Comment(1L, COMMENT_CONTENT, COMMENT_DATE, POST_ID, USER_ID)));
        when(userDisplay.usernamesOf(Set.of(USER_ID))).thenReturn(Map.of(USER_ID, AUTHOR));

        List<CommentResponse> comments = commentService.list(POST_ID);

        assertThat(comments).containsExactly(
                new CommentResponse(1L, COMMENT_CONTENT, COMMENT_DATE, AUTHOR, POST_ID));
    }
}
