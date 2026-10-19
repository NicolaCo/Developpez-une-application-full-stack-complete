package com.openclassrooms.mddapi.authentification.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

    private static final String SECRET = "pZRrsWUFV/kDxTvqIiSneULHDYUsRb9ATEDs9rXzfd10Dp8mW8aqmLbKqHKOYDfmFrCayGSc4/QPUtxvubQaBg==";
    private static final String OTHER_SECRET = "TvU7dcTlXOW0ZS9HjTZIF0e0VEwF4x/2JPIPMj3PaHv5CskumZL4BQBCaYGp2qSPSCn5vocOARfSF4hEdLfwOg==";
    private static final long EXPIRATION_MS = 3_600_000L;
    private static final String EMAIL = "user@example.com";
    private static final String USERNAME = "testuser";
    private static final long USER_ID = 42L;
    private static final String OTHER_EMAIL = "other@example.com";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION_MS);
    }

    @Test
    void generateToken_whenValid_setsSubjectEmail() {
        String token = jwtService.generateToken(EMAIL, USER_ID, USERNAME);

        assertThat(jwtService.extractSubject(token)).isEqualTo(EMAIL);
    }

    @Test
    void generateToken_whenValid_setsIdEmailAndUsernameClaims() {
        String token = jwtService.generateToken(EMAIL, USER_ID, USERNAME);

        assertThat(jwtService.extractId(token)).isEqualTo(USER_ID);
        assertThat(jwtService.extractEmail(token)).isEqualTo(EMAIL);
        assertThat(jwtService.extractUsername(token)).isEqualTo(USERNAME);
    }

    @Test
    void isTokenValid_whenSubjectMatchesAndNotExpired_returnsTrue() {
        String token = jwtService.generateToken(EMAIL, USER_ID, USERNAME);

        assertThat(jwtService.isTokenValid(token, EMAIL)).isTrue();
    }

    @Test
    void isTokenValid_whenSubjectDiffers_returnsFalse() {
        String token = jwtService.generateToken(EMAIL, USER_ID, USERNAME);

        assertThat(jwtService.isTokenValid(token, OTHER_EMAIL)).isFalse();
    }

    @Test
    void isTokenValid_whenExpired_returnsFalse() {
        ReflectionTestUtils.setField(jwtService, "expirationMs", -1_000L);
        String token = jwtService.generateToken(EMAIL, USER_ID, USERNAME);

        assertThat(jwtService.isTokenValid(token, EMAIL)).isFalse();
    }

    @Test
    void isTokenValid_whenSignedWithAnotherKey_returnsFalse() {
        JwtService otherService = new JwtService(OTHER_SECRET, EXPIRATION_MS);
        String token = otherService.generateToken(EMAIL, USER_ID, USERNAME);

        assertThat(jwtService.isTokenValid(token, EMAIL)).isFalse();
    }
}
