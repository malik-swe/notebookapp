package com.example.notebookapp.auth;

import com.example.notebookapp.security.token.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class LogoutController {

    private static final Logger log = LoggerFactory.getLogger(LogoutController.class);
    private final RefreshTokenService refreshTokenService;

    public LogoutController(RefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        // Get authenticated user before clearing context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = null;

        if (auth != null && auth.isAuthenticated()) {
            email = auth.getName();
        }

        // Revoke all refresh tokens for this user
        if (email != null) {
            refreshTokenService.revokeAllUserTokens(email);
            log.info("User logged out, tokens revoked: {}", email);
        }

        // Clear Spring Security context
        SecurityContextHolder.clearContext();

        // Invalidate HTTP session if it exists
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}