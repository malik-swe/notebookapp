package com.example.notebookapp.controller;

import com.example.notebookapp.dto.CreateUserRequest;
import com.example.notebookapp.dto.LoginRequest;
import com.example.notebookapp.model.User;
import com.example.notebookapp.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // =========================
    // JSON REGISTRATION
    // =========================
    @PostMapping("/register")
    public ResponseEntity<?> registerJson(
            @RequestHeader("Content-Type") String contentType,
            @Valid @RequestBody CreateUserRequest request
    ) {

        if (!contentType.equalsIgnoreCase("application/json")) {
            return ResponseEntity
                    .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body("Only application/json is supported");
        }

        User user = userService.createUser(
                request.getUsername(),
                request.getEmail(),
                request.getPassword()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    // =========================
    // FORM REGISTRATION
    // =========================
    @PostMapping(
            value = "/register-form",
            consumes = "application/x-www-form-urlencoded"
    )
    public ResponseEntity<User> registerForm(
            @Valid @ModelAttribute CreateUserRequest request
    ) {

        User user = userService.createUser(
                request.getUsername(),
                request.getEmail(),
                request.getPassword()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    // =========================
    // JSON LOGIN
    // =========================
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestHeader("Content-Type") String contentType,
            @RequestBody LoginRequest request
    ) {

        if (!contentType.equalsIgnoreCase("application/json")) {
            return ResponseEntity
                    .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body("Only application/json is supported");
        }

        User user = userService.authenticate(
                request.getEmail(),
                request.getPassword()
        );

        return ResponseEntity.ok(user);
    }
}
