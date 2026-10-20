package com.openclassrooms.mddapi.topic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.openclassrooms.mddapi.common.dto.ApiError;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class TopicExceptionHandler {

    @ExceptionHandler(TopicNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleTopicNotFound(TopicNotFoundException exception) {
        log.warn("404 Resource not found: {}", exception.getMessage());
        return ApiError.of(HttpStatus.NOT_FOUND.value(), exception.getMessage());
    }

    @ExceptionHandler(AlreadySubscribedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleAlreadySubscribed(AlreadySubscribedException exception) {
        log.error("409 Subscription rejected: {}", exception.getMessage());
        return ApiError.of(HttpStatus.CONFLICT.value(), exception.getMessage());
    }
}
