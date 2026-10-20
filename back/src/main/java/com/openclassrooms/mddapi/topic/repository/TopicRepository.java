package com.openclassrooms.mddapi.topic.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.openclassrooms.mddapi.topic.model.Topic;

public interface TopicRepository extends JpaRepository<Topic, Long> {
}
