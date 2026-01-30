package com.example.notebookapp.security.token;

import com.example.notebookapp.model.User;
import com.example.notebookapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Service
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);
    private final RefreshTokenRepository repository;
    private final UserRepository userRepository;
    private final long refreshExpirationMs;
    private final SecureRandom random = new SecureRandom();

    public RefreshTokenService(
            RefreshTokenRepository repository,
            UserRepository userRepository,
            @Value("${jwt.refresh-expiration}") long refreshExpirationMs
    ) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    /**
     * Create a new refresh token for a user
     */
    public String create(User user) {
        byte[] bytes = new byte[64];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        RefreshToken refreshToken = new RefreshToken(
                token,
                user,
                Instant.now().plusMillis(refreshExpirationMs)
        );

        repository.save(refreshToken);
        log.info("Created refresh token for user={}", user.getEmail());
        return token;
    }

    /**
     * Validate and consume a refresh token (rotation pattern)
     * Returns the user email if valid, null otherwise
     * Automatically revokes the old token
     */
    public String validateAndRotate(String token) {
        Optional<RefreshToken> optToken = repository.findByToken(token);

        if (optToken.isEmpty()) {
            log.warn("Refresh token not found");
            return null;
        }

        RefreshToken refreshToken = optToken.get();

        // Check if already revoked
        if (refreshToken.isRevoked()) {
            log.warn("Attempted to use revoked refresh token for user={}",
                    refreshToken.getUser().getEmail());
            return null;
        }

        // Check if expired
        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            log.warn("Attempted to use expired refresh token for user={}",
                    refreshToken.getUser().getEmail());
            // Revoke expired token
            refreshToken.revoke();
            repository.save(refreshToken);
            return null;
        }

        // Token is valid - revoke it (part of rotation)
        refreshToken.revoke();
        repository.save(refreshToken);

        log.info("Validated and revoked refresh token for user={}",
                refreshToken.getUser().getEmail());

        return refreshToken.getUser().getEmail();
    }

    /**
     * Revoke all refresh tokens for a user (used during logout)
     */
    public void revokeAllUserTokens(String email) {
        Optional<User> optUser = userRepository.findByEmail(email);
        if (optUser.isEmpty()) {
            log.warn("Cannot revoke tokens - user not found: {}", email);
            return;
        }

        User user = optUser.get();
        repository.revokeAllByUser(user);
        log.info("Revoked all refresh tokens for user={}", email);
    }
}