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
import com.openclassrooms.mddapi.dto.post.CreatePostRequest;
import com.openclassrooms.mddapi.dto.post.PostResponse;
import com.openclassrooms.mddapi.exception.PostNotFoundException;
import com.openclassrooms.mddapi.exception.TopicNotFoundException;
import com.openclassrooms.mddapi.model.Post;
import com.openclassrooms.mddapi.repository.PostRepository;
import com.openclassrooms.mddapi.model.Subscription;
import com.openclassrooms.mddapi.repository.SubscriptionRepository;
import com.openclassrooms.mddapi.repository.TopicRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final TopicRepository topicRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SecurityCurrentUserProvider currentUserProvider;
    private final UserDisplay userDisplay;

    @Transactional
    public PostResponse create(CreatePostRequest request) {
        if (!topicRepository.existsById(request.topicId())) {
            throw new TopicNotFoundException(request.topicId());
        }
        Long userId = currentUserProvider.currentUserId();
        Post post = postRepository.save(
                new Post(request.title(), request.content(), Instant.now(), request.topicId(), userId));
        return toResponse(post, Map.of(userId, usernameOf(userId)));
    }

    @Transactional(readOnly = true)
    public List<PostResponse> list(Sort.Direction direction) {
        Long userId = currentUserProvider.currentUserId();
        Set<Long> subscribedTopicIds = subscriptionRepository.findAllByUserId(userId).stream()
                .map(Subscription::getTopicId)
                .collect(Collectors.toSet());
        if (subscribedTopicIds.isEmpty()) {
            return List.of();
        }
        List<Post> posts = postRepository.findAllByTopicIdIn(subscribedTopicIds, Sort.by(direction, "date"));
        Map<Long, String> authors = userDisplay.usernamesOf(
                posts.stream().map(Post::getUserId).collect(Collectors.toSet()));
        return posts.stream().map(post -> toResponse(post, authors)).toList();
    }

    @Transactional(readOnly = true)
    public PostResponse detail(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id));
        return toResponse(post, userDisplay.usernamesOf(Set.of(post.getUserId())));
    }

    private String usernameOf(Long userId) {
        return userDisplay.usernamesOf(Set.of(userId)).get(userId);
    }

    private PostResponse toResponse(Post post, Map<Long, String> authors) {
        return new PostResponse(post.getId(), post.getTitle(), post.getContent(), post.getDate(),
                authors.get(post.getUserId()), post.getTopicId());
    }
}
