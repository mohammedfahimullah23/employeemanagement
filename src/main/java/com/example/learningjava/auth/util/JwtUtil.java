package com.example.learningjava.auth.util;

import com.example.learningjava.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;


@Component
public class JwtUtil {

    private final SecretKey key;

    public JwtUtil(
            @Value("${jwt.secret}") String secret
    ) {
        this.key = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret)
        );
    }

    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .subject(user.getEmail())              // who the user is
                .issuedAt(new Date())                  // token issued time
                .expiration(
                        new Date(System.currentTimeMillis() + 15 * 60 * 1000)) // 15 min
                .signWith(key)                         // HS256 inferred from key
                .compact();
    }

}


