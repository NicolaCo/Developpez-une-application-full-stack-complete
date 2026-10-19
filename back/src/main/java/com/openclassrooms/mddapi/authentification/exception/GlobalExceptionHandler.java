package com.openclassrooms.mddapi.authentification.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.openclassrooms.mddapi.authentification.dto.ApiError;

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
