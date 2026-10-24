package com.openclassrooms.mddapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.openclassrooms.mddapi.model.Subscription;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByUserIdAndTopicId(Long userId, Long topicId);

    List<Subscription> findAllByUserId(Long userId);

    void deleteByUserIdAndTopicId(Long userId, Long topicId);
}
