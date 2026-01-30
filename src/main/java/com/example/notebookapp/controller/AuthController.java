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

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository repo;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    private final RefreshTokenService refreshTokenService;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    public AuthController(UserRepository repo, JwtUtil jwtUtil, RefreshTokenService refreshTokenService) {
        this.repo = repo;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {

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

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        ));
    }
}
