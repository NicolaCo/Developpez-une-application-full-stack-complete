package com.openclassrooms.mddapi.dto.user;

public record UpdatedUserProfileResponse(
        Long id,
        String email,
        String username,
        String token) {
}
