package com.example.notebookapp.controller;

import com.example.notebookapp.model.User;
import com.example.notebookapp.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Register a new user
    @PostMapping("/register")
    public ResponseEntity<User> register(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password
    ) {
        User createdUser = userService.createUser(username, email, password);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    // Login / authenticate
    @PostMapping("/login")
    public ResponseEntity<User> login(
            @RequestParam String email,
            @RequestParam String password
    ) {
        User user = userService.authenticate(email, password);
        return ResponseEntity.ok(user);
    }
}
