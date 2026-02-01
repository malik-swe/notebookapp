package com.example.notebookapp.auth;

import com.example.notebookapp.security.token.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
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
    public ResponseEntity<?> logout(Authentication auth, HttpServletResponse response) {
        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName();

            // Revoke all refresh tokens for this user
            refreshTokenService.revokeAllUserTokens(email);

            log.info("User logged out: {}", email);
        }

        // create new immediately expired cookie
        Cookie accessTokenCookie = new Cookie("accessToken", "");
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0); // Expire immediately
        response.addCookie(accessTokenCookie);

        // clear cookies by replacing it with immediately expired cookie
        Cookie refreshTokenCookie = new Cookie("refreshToken", "");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0); // Expire immediately
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}