package com.openclassrooms.mddapi.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.openclassrooms.mddapi.security.SecurityCurrentUserProvider;
import com.openclassrooms.mddapi.dto.post.CommentResponse;
import com.openclassrooms.mddapi.dto.post.CreateCommentRequest;
import com.openclassrooms.mddapi.exception.PostNotFoundException;
import com.openclassrooms.mddapi.model.Comment;
import com.openclassrooms.mddapi.repository.CommentRepository;
import com.openclassrooms.mddapi.repository.PostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final SecurityCurrentUserProvider currentUserProvider;
    private final UserDisplay userDisplay;

    @Transactional
    public CommentResponse create(Long postId, CreateCommentRequest request) {
        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException(postId);
        }
        Long userId = currentUserProvider.currentUserId();
        Comment comment = commentRepository.save(
                new Comment(request.content(), Instant.now(), postId, userId));
        return toResponse(comment, Map.of(userId, usernameOf(userId)));
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> list(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException(postId);
        }
        List<Comment> comments = commentRepository.findAllByPostId(postId, Sort.by(Sort.Direction.ASC, "date"));
        Map<Long, String> authors = userDisplay.usernamesOf(
                comments.stream().map(Comment::getUserId).collect(Collectors.toSet()));
        return comments.stream().map(comment -> toResponse(comment, authors)).toList();
    }

    private String usernameOf(Long userId) {
        return userDisplay.usernamesOf(Set.of(userId)).get(userId);
    }

    private CommentResponse toResponse(Comment comment, Map<Long, String> authors) {
        return new CommentResponse(comment.getId(), comment.getContent(), comment.getDate(),
                authors.get(comment.getUserId()), comment.getPostId());
    }
}
