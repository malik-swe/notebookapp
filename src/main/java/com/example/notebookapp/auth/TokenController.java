package com.example.notebookapp.auth;

import com.example.notebookapp.security.jwt.JwtUtil;
import com.example.notebookapp.security.token.RefreshTokenService;
import com.example.notebookapp.repository.UserRepository;
import com.example.notebookapp.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class TokenController {

    private static final Logger log = LoggerFactory.getLogger(TokenController.class);
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    public TokenController(JwtUtil jwtUtil, RefreshTokenService refreshTokenService, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            log.warn("Refresh attempt with missing token");
            return ResponseEntity.badRequest().body(Map.of("error", "Missing refresh token"));
        }

        // Validate and rotate the refresh token
        String email = refreshTokenService.validateAndRotate(refreshToken);
        if (email == null) {
            log.warn("Refresh attempt with invalid/expired token");
            return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired refresh token"));
        }

        // Get the user to create a new refresh token
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found after token validation"));

        // Generate new tokens (rotation)
        String newAccessToken = jwtUtil.generateToken(email);
        String newRefreshToken = refreshTokenService.create(user);

        log.info("Successfully refreshed tokens for user={}", email);

        return ResponseEntity.ok(Map.of(
                "accessToken", newAccessToken,
                "refreshToken", newRefreshToken
        ));
    }
}