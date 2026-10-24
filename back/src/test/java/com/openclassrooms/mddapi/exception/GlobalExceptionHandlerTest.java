package com.openclassrooms.mddapi.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import com.openclassrooms.mddapi.dto.ApiError;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleDataIntegrityViolation_returnsConflictApiError() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException("Duplicate entry",
                new SQLException("Duplicate entry 'user@example.com' for key 'users.email'"));

        ApiError error = handler.handleDataIntegrityViolation(exception);

        assertThat(error.status()).isEqualTo(409);
        assertThat(error.message()).isEqualTo("Resource conflict");
    }
}
