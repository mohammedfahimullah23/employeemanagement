package com.example.learningjava.auth.service;

import com.example.learningjava.auth.model.RefreshToken;
import com.example.learningjava.auth.repository.RefreshTokenRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder encoder;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               PasswordEncoder encoder) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.encoder = encoder;
    }

    public String createRefreshToken(Long userId) {

        String rawToken = UUID.randomUUID().toString();

        RefreshToken token = new RefreshToken();
        token.setUserId(userId);
        token.setTokenHash(encoder.encode(rawToken));
        token.setExpiresAt(LocalDateTime.now().plusDays(7));
        token.setRevoked(false);

        refreshTokenRepository.save(token);
        return rawToken;
    }

    public RefreshToken validateRefreshToken(String rawToken) {
        return refreshTokenRepository.findAll().stream()
                .filter(t -> !t.isRevoked())
                .filter(t -> encoder.matches(rawToken, t.getTokenHash()))
                .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
    }

    public void revokeToken(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }
}

