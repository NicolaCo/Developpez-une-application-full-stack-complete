package com.openclassrooms.mddapi.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.StringUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.openclassrooms.mddapi.dto.ApiError;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        log.warn("400 Validation failed: {}", fieldErrors);
        return ApiError.of(HttpStatus.BAD_REQUEST.value(), "Validation failed");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleUnreadableBody(HttpMessageNotReadableException exception) {
        log.warn("400 Malformed request body: {}", exception.getMessage());
        return ApiError.of(HttpStatus.BAD_REQUEST.value(), "Malformed request body");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleArgumentTypeMismatch(MethodArgumentTypeMismatchException exception) {
        log.warn("400 Invalid request parameter '{}': {}", exception.getName(), exception.getMessage());
        return ApiError.of(HttpStatus.BAD_REQUEST.value(), "Invalid request parameter");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ApiError handleMethodNotSupported(HttpRequestMethodNotSupportedException exception,
            HttpServletResponse response) {
        response.setHeader(HttpHeaders.ALLOW,
                StringUtils.collectionToCommaDelimitedString(exception.getSupportedHttpMethods()));
        log.warn("405 Method not allowed: {}", exception.getMessage());
        return ApiError.of(HttpStatus.METHOD_NOT_ALLOWED.value(), "Method not allowed");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public ApiError handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException exception,
            HttpServletResponse response) {
        response.setHeader(HttpHeaders.ACCEPT,
                StringUtils.collectionToCommaDelimitedString(exception.getSupportedMediaTypes()));
        log.warn("415 Unsupported media type: {}", exception.getMessage());
        return ApiError.of(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), "Unsupported media type");
    }

    @ExceptionHandler(PostNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handlePostNotFound(PostNotFoundException exception) {
        log.warn("404 Post not found: {}", exception.getMessage());
        return ApiError.of(HttpStatus.NOT_FOUND.value(), "Post not found");
    }

    @ExceptionHandler(TopicNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleTopicNotFound(TopicNotFoundException exception) {
        log.warn("404 Topic not found: {}", exception.getMessage());
        return ApiError.of(HttpStatus.NOT_FOUND.value(), "Topic not found");
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleUserNotFound(UserNotFoundException exception) {
        log.warn("404 User not found: {}", exception.getMessage());
        return ApiError.of(HttpStatus.NOT_FOUND.value(), "User not found");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNoResourceFound(NoResourceFoundException exception) {
        log.warn("404 Resource not found: {}", exception.getResourcePath());
        return ApiError.of(HttpStatus.NOT_FOUND.value(), "Resource not found");
    }

    @ExceptionHandler(AlreadySubscribedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleAlreadySubscribed(AlreadySubscribedException exception) {
        log.error("409 Subscription rejected: {}", exception.getMessage());
        return ApiError.of(HttpStatus.CONFLICT.value(), "Already subscribed to this topic");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        log.error("409 Data integrity violation: {}", exception.getMostSpecificCause().getMessage());
        return ApiError.of(HttpStatus.CONFLICT.value(), "Resource conflict");
    }
}
