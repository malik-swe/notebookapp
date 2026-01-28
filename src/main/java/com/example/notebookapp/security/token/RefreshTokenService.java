package com.example.notebookapp.security.token;

import com.example.notebookapp.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final long refreshExpirationMs;
    private final SecureRandom random = new SecureRandom();

    public RefreshTokenService(
            RefreshTokenRepository repository,
            @Value("${jwt.refresh-expiration}") long refreshExpirationMs
    ) {
        this.repository = repository;
        this.refreshExpirationMs = refreshExpirationMs;
    }

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
        return token;
    }
}
