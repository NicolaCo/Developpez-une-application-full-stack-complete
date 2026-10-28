package com.openclassrooms.mddapi.dto.user;

public record UserProfileResponse(
        Long id,
        String email,
        String username) {
}
