package com.openclassrooms.mddapi.dto.topic;

public record TopicResponse(
        Long id,
        String title,
        String description,
        boolean isSubscribed) {
}
