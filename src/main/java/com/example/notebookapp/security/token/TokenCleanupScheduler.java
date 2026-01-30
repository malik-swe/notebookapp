package com.example.notebookapp.security.token;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

@Component
public class TokenCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(TokenCleanupScheduler.class);
    private final RefreshTokenRepository repository;

    public TokenCleanupScheduler(RefreshTokenRepository repository) {
        this.repository = repository;
    }

    // Run every day at 3 AM
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Starting cleanup of expired refresh tokens");
        repository.deleteExpiredAndRevoked(Instant.now());
        log.info("Finished cleanup of expired refresh tokens");
    }
}