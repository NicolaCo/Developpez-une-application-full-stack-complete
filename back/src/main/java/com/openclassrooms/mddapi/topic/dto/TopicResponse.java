package com.openclassrooms.mddapi.topic.dto;

public record TopicResponse(
        Long id,
        String title,
        String description,
        boolean isSubscribed) {
}
