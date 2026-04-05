package com.logarithm.microtask.security;

import com.logarithm.microtask.entity.RevokedToken;
import com.logarithm.microtask.repository.RevokedTokenRepository;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TokenRevocationService {

    private final RevokedTokenRepository revokedTokenRepository;
    private final JwtService jwtService;

    @Transactional
    public void revokeToken(String token) {
        LocalDateTime expiresAt;
        try {
            expiresAt = jwtService.extractExpiration(token);
        } catch (JwtException | IllegalArgumentException ex) {
            return;
        }

        if (expiresAt.isBefore(LocalDateTime.now())) {
            return;
        }

        String tokenHash = hashToken(token);
        if (revokedTokenRepository.existsByTokenHash(tokenHash)) {
            return;
        }

        revokedTokenRepository.save(RevokedToken.builder()
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .build());
    }

    @Transactional(readOnly = true)
    public boolean isRevoked(String token) {
        return revokedTokenRepository.existsByTokenHash(hashToken(token));
    }

    @Transactional
    @Scheduled(cron = "0 0 * * * *")
    public void purgeExpiredRevocations() {
        revokedTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : hashed) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm not available", ex);
        }
    }
}