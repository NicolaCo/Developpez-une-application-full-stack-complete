package com.openclassrooms.mddapi.dto.post;

import java.time.Instant;

public record PostResponse(
        Long id,
        String title,
        String content,
        Instant date,
        String author,
        Long topicId) {
}
