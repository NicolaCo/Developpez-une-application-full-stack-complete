package com.openclassrooms.mddapi.dto.post;

import jakarta.validation.constraints.NotBlank;

public record CreateCommentRequest(
        @NotBlank String content) {
}
