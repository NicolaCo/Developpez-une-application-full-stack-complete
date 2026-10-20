package com.openclassrooms.mddapi.common.dto;

public record ApiError(
        int status,
        String message) {

    public static ApiError of(int status, String message) {
        return new ApiError(status, message);
    }
}
