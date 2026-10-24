package com.openclassrooms.mddapi.dto.post;

import java.time.Instant;

public record CommentResponse(
        Long id,
        String content,
        Instant date,
        String author,
        Long postId) {
}
