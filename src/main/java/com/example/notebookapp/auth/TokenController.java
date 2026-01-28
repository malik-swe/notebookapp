package com.example.notebookapp.auth;

import com.example.notebookapp.security.jwt.JwtUtil;
import com.example.notebookapp.security.jwt.RefreshTokenStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class TokenController {

    private final JwtUtil jwtUtil;
    private final RefreshTokenStore store;

    public TokenController(JwtUtil jwtUtil, RefreshTokenStore store) {
        this.jwtUtil = jwtUtil;
        this.store = store;
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {

        String refreshToken = body.get("refreshToken");
        if (refreshToken == null) {
            return ResponseEntity.badRequest().body("Missing refresh token");
        }

        String email = store.consume(refreshToken);
        if (email == null) {
            return ResponseEntity.status(401).body("Invalid refresh token");
        }

        String newAccessToken = jwtUtil.generateToken(email);
        String newRefreshToken = store.create(email);

        return ResponseEntity.ok(Map.of(
                "accessToken", newAccessToken,
                "refreshToken", newRefreshToken
        ));
    }
}
