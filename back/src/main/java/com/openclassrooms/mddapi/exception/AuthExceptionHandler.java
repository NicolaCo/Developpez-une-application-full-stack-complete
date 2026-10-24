package com.openclassrooms.mddapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.openclassrooms.mddapi.dto.ApiError;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError handleAuthentication(AuthenticationException exception) {
        log.error("401 Authentication failed: {}", exception.getMessage());
        return ApiError.of(HttpStatus.UNAUTHORIZED.value(), "Invalid credentials");
    }

    @ExceptionHandler(EmailAlreadyUsedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleEmailAlreadyUsed(EmailAlreadyUsedException exception) {
        log.error("409 Registration rejected: {}", exception.getMessage());
        return ApiError.of(HttpStatus.CONFLICT.value(), exception.getMessage());
    }

    @ExceptionHandler(UsernameAlreadyUsedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleUsernameAlreadyUsed(UsernameAlreadyUsedException exception) {
        log.error("409 Registration rejected: {}", exception.getMessage());
        return ApiError.of(HttpStatus.CONFLICT.value(), exception.getMessage());
    }
}
