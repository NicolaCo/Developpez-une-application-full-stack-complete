package com.openclassrooms.mddapi.topic.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.openclassrooms.mddapi.common.user.CurrentUserPort;
import com.openclassrooms.mddapi.topic.dto.TopicResponse;
import com.openclassrooms.mddapi.topic.exception.AlreadySubscribedException;
import com.openclassrooms.mddapi.topic.exception.TopicNotFoundException;
import com.openclassrooms.mddapi.topic.model.Subscription;
import com.openclassrooms.mddapi.topic.model.Topic;
import com.openclassrooms.mddapi.topic.repository.SubscriptionRepository;
import com.openclassrooms.mddapi.topic.repository.TopicRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final CurrentUserPort currentUserProvider;

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
