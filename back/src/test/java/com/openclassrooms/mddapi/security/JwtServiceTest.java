package com.openclassrooms.mddapi.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

    private static final String SECRET = "b5qLr3MBT93sn4xES1yXyNdCay2jRfJjqZQm80MwqEHhB/8Ec/XxiG48KHWhLcIFlFI3g01asbm+cc7pV5rHnw==";
    private static final String OTHER_SECRET = "rnCZoPOL+sKgvQMuUAbaXimYVztaHkthAE2pgwBM4NnEkUFoP+aNwUIAuanttT/UgOGgLjExKCZgKNbayBsE+Q==";
    private static final long EXPIRATION_MS = 3_600_000L;
    private static final String EMAIL = "user@example.com";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION_MS);
    }

    @Test
    void generateToken_whenValid_returnsTokenWithEmailSubject() {
        String token = jwtService.generateToken(EMAIL);

        assertThat(jwtService.extractValidSubject(token)).contains(EMAIL);
    }

    @Test
    void extractValidSubject_whenExpired_returnsEmpty() {
        ReflectionTestUtils.setField(jwtService, "expirationMs", -1_000L);
        String token = jwtService.generateToken(EMAIL);

        assertThat(jwtService.extractValidSubject(token)).isEmpty();
    }

    @Test
    void extractValidSubject_whenSignedWithAnotherKey_returnsEmpty() {
        JwtService otherService = new JwtService(OTHER_SECRET, EXPIRATION_MS);
        String token = otherService.generateToken(EMAIL);

        assertThat(jwtService.extractValidSubject(token)).isEmpty();
    }

    @Test
    void extractValidSubject_whenMalformed_returnsEmpty() {
        assertThat(jwtService.extractValidSubject("not.a.jwt")).isEmpty();
    }
}
