package com.logarithm.microtask.repository;

import com.logarithm.microtask.entity.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {
    boolean existsByTokenHash(String tokenHash);

    long deleteByExpiresAtBefore(LocalDateTime timestamp);
}