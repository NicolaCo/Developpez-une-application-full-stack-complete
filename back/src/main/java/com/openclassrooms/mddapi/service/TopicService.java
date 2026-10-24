package com.openclassrooms.mddapi.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.openclassrooms.mddapi.security.SecurityCurrentUserProvider;
import com.openclassrooms.mddapi.dto.topic.TopicResponse;
import com.openclassrooms.mddapi.exception.AlreadySubscribedException;
import com.openclassrooms.mddapi.exception.TopicNotFoundException;
import com.openclassrooms.mddapi.model.Subscription;
import com.openclassrooms.mddapi.model.Topic;
import com.openclassrooms.mddapi.repository.SubscriptionRepository;
import com.openclassrooms.mddapi.repository.TopicRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SecurityCurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public List<TopicResponse> list() {
        Long userId = currentUserProvider.currentUserId();
        Set<Long> subscribedTopicIds = subscriptionRepository.findAllByUserId(userId).stream()
                .map(Subscription::getTopicId)
                .collect(Collectors.toSet());
        return topicRepository.findAll(Sort.by("id")).stream()
                .map(topic -> toResponse(topic, subscribedTopicIds.contains(topic.getId())))
                .toList();
    }

    @Transactional
    public void subscribe(Long topicId) {
        Long userId = currentUserProvider.currentUserId();
        if (!topicRepository.existsById(topicId)) {
            throw new TopicNotFoundException(topicId);
        }
        if (subscriptionRepository.findByUserIdAndTopicId(userId, topicId).isPresent()) {
            throw new AlreadySubscribedException();
        }
        subscriptionRepository.save(new Subscription(userId, topicId));
    }

    @Transactional
    public void unsubscribe(Long topicId) {
        Long userId = currentUserProvider.currentUserId();
        if (!topicRepository.existsById(topicId)) {
            throw new TopicNotFoundException(topicId);
        }
        subscriptionRepository.deleteByUserIdAndTopicId(userId, topicId);
    }

    private TopicResponse toResponse(Topic topic, boolean isSubscribed) {
        return new TopicResponse(topic.getId(), topic.getTitle(), topic.getDescription(), isSubscribed);
    }
}
