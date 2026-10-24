package com.openclassrooms.mddapi.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePostRequest(
        @NotBlank String title,
        @NotBlank String content,
        @NotNull Long topicId) {
}
