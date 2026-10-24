package com.openclassrooms.mddapi.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AuthUserTest {

    private static final long USER_ID = 3L;
    private static final String EMAIL = "user@example.com";
    private static final String ENCODED_PASSWORD = "$2a$10$encoded";

    private final AuthUser user = new AuthUser(USER_ID, EMAIL, ENCODED_PASSWORD);

    @Test
    void getUsername_exposesEmailAsUsername() {
        assertThat(user.getUsername()).isEqualTo(EMAIL);
        assertThat(user.getPassword()).isEqualTo(ENCODED_PASSWORD);
        assertThat(user.getAuthorities()).isEmpty();
    }

    @Test
    void toString_masksPassword() {
        assertThat(user.toString())
                .contains(EMAIL)
                .contains(String.valueOf(USER_ID))
                .doesNotContain(ENCODED_PASSWORD)
                .contains("password=***");
    }
}
