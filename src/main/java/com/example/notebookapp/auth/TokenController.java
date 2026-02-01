package com.example.notebookapp.auth;

import com.example.notebookapp.security.jwt.JwtUtil;
import com.example.notebookapp.security.token.RefreshTokenService;
import com.example.notebookapp.repository.UserRepository;
import com.example.notebookapp.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;
import java.util.Arrays;

@RestController
@RequestMapping("/auth")
public class TokenController {
    private static final Logger log = LoggerFactory.getLogger(TokenController.class);
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    private static final int ACCESS_TOKEN_COOKIE_MAX_AGE = 15 * 60; // 15 minutes in seconds
    private static final int REFRESH_TOKEN_COOKIE_MAX_AGE = 7 * 24 * 60 * 60; // 7 days in seconds

    public TokenController(JwtUtil jwtUtil, RefreshTokenService refreshTokenService, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        // extract refresh token from cookie
        String refreshToken = extractRefreshTokenFromCookie(request);

        if (refreshToken == null || refreshToken.isBlank()) {
            log.warn("Refresh attempt with missing token");
            return ResponseEntity.badRequest().body(Map.of("error", "Missing refresh token"));
        }

        // validate and rotate the refresh token
        String email = refreshTokenService.validateAndRotate(refreshToken);
        if (email == null) {
            log.warn("Refresh attempt with invalid/expired token");
            return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired refresh token"));
        }

        // get the user to create a new refresh token
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found after token validation"));

        // generate new tokens
        String newAccessToken = jwtUtil.generateToken(email);
        String newRefreshToken = refreshTokenService.create(user);

        // set new access token cookie
        Cookie accessTokenCookie = createCookie("accessToken", newAccessToken, ACCESS_TOKEN_COOKIE_MAX_AGE);
        response.addCookie(accessTokenCookie);

        // set new refresh token cookie
        Cookie refreshTokenCookie = createCookie("refreshToken", newRefreshToken, REFRESH_TOKEN_COOKIE_MAX_AGE);
        response.addCookie(refreshTokenCookie);

        log.info("Successfully refreshed tokens for user={}", email);

        return ResponseEntity.ok(Map.of(
                "message", "Tokens refreshed successfully",
                "email", email
        ));
    }

    // extract refresh token from cookies
    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        return Arrays.stream(cookies)
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    // create secure cookies with all required security flags
    private Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);      // Prevents JavaScript access
        cookie.setSecure(true);         // Only sent over HTTPS
        cookie.setPath("/");            // Available for entire application
        cookie.setMaxAge(maxAge);       // Expiration time
        cookie.setAttribute("SameSite", "Strict"); // CSRF protection
        return cookie;
    }
}