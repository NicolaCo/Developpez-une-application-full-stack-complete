package com.openclassrooms.mddapi.topic.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.openclassrooms.mddapi.topic.model.Subscription;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByUserIdAndTopicId(Long userId, Long topicId);

    List<Subscription> findAllByUserId(Long userId);

    void deleteByUserIdAndTopicId(Long userId, Long topicId);
}
