package com.openclassrooms.mddapi.authentification.security;

import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private long expirationMs;

    public JwtService(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") long expirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationMs = expirationMs;
    }

    public String generateToken(String email, Long id, String username) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(email)
                .claim("id", id)
                .claim("email", email)
                .claim("username", username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(signingKey, Jwts.SIG.HS512)
                .compact();
    }

    public String extractSubject(String token) {
        return parse(token).getSubject();
    }

    public Long extractId(String token) {
        return parse(token).get("id", Number.class).longValue();
    }

    public String extractEmail(String token) {
        return parse(token).get("email", String.class);
    }

    public String extractUsername(String token) {
        return parse(token).get("username", String.class);
    }

    public boolean isTokenValid(String token, String subject) {
        try {
            Claims claims = parse(token);
            return claims.getSubject().equals(subject)
                    && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
