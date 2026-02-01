package com.example.notebookapp.controller;

import com.example.notebookapp.dto.LoginRequest;
import com.example.notebookapp.model.User;
import com.example.notebookapp.repository.UserRepository;
import com.example.notebookapp.security.jwt.JwtUtil;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.example.notebookapp.security.token.RefreshTokenService;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository repo;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    private final RefreshTokenService refreshTokenService;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private static final int ACCESS_TOKEN_COOKIE_MAX_AGE = 15 * 60; // 15 minutes
    private static final int REFRESH_TOKEN_COOKIE_MAX_AGE = 7 * 24 * 60 * 60; // 7 days in seconds

    public AuthController(UserRepository repo, JwtUtil jwtUtil, RefreshTokenService refreshTokenService) {
        this.repo = repo;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpServletResponse response) {

        User user = repo.findByEmail(req.getEmail())
                .orElse(null);

        if (user == null || !encoder.matches(req.getPassword(), user.getPassword())) {
            log.warn("Failed login attempt for email={}", req.getEmail());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        }

        String accessToken = jwtUtil.generateToken(user.getEmail());
        String refreshToken = refreshTokenService.create(user);

        // Set access token cookie
        Cookie accessTokenCookie = createCookie("accessToken", accessToken, ACCESS_TOKEN_COOKIE_MAX_AGE);
        response.addCookie(accessTokenCookie);

        // Set refresh token cookie
        Cookie refreshTokenCookie = createCookie("refreshToken", refreshToken, REFRESH_TOKEN_COOKIE_MAX_AGE);
        response.addCookie(refreshTokenCookie);

        log.info("Successful login for user={}", user.getEmail());

        return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "email", user.getEmail()
        ));
    }

    private Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);       // Prevents JavaScript access
        cookie.setSecure(true);         // Only sent over HTTPS
        cookie.setPath("/");            // Available for entire application
        cookie.setMaxAge(maxAge);       // Expiration time
        cookie.setAttribute("SameSite", "Strict"); // CSRF protection
        return cookie;
    }
}