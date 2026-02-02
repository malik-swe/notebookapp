package com.example.notebookapp.integration;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import tools.jackson.databind.ObjectMapper;
import com.example.notebookapp.dto.CreateUserRequest;
import com.example.notebookapp.dto.LoginRequest;
import com.example.notebookapp.model.User;
import com.example.notebookapp.repository.UserRepository;
import com.example.notebookapp.security.token.RefreshTokenRepository;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthenticationIntegrationTest {

    @Autowired private MockMvc          mockMvc;
    @Autowired private ObjectMapper     objectMapper;
    @Autowired private UserRepository   userRepository;
    @Autowired private RefreshTokenRepository refreshTokenRepository;
    
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @BeforeEach
    void wipe() {
        // refresh_tokens has FK -> users, must go first
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    // REGISTRATION  (POST /users/register)

    @Test
    void register_passwordTooShort_returns400WithErrorResponse() throws Exception {
        CreateUserRequest body = new CreateUserRequest();
        body.setUsername("bob");
        body.setEmail("bob@test.com");
        body.setPassword("Short1!");       // only 7 chars, fails ≥12 rule

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                // GlobalExceptionHandler -> ErrorResponse fields:
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").isString());
    }

    // LOGIN  (POST /auth/login  -  AuthController)

    @Test
    void login_correctCredentials_returns200WithCookiesAndMessage() throws Exception {
        String email = "login@test.com";
        String pass  = "LoginP@ss1";
        userRepository.save(new User("loginuser", email, encoder.encode(pass)));

        LoginRequest req = new LoginRequest();
        req.setEmail(email);
        req.setPassword(pass);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                // AuthController body: Map.of("message","Login successful","email",...)
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.email").value(email))
                // cookies set by AuthController.createCookie()
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().httpOnly("accessToken", true))
                .andExpect(cookie().httpOnly("refreshToken", true));
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        userRepository.save(
                new User("wpuser", "wp@test.com", encoder.encode("RightPass1!")));

        LoginRequest req = new LoginRequest();
        req.setEmail("wp@test.com");
        req.setPassword("WrongPass1!");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                // AuthController body: Map.of("error","Invalid credentials")
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    @Test
    void login_unknownEmail_returns401() throws Exception {
        // no user seeded - email does not exist
        LoginRequest req = new LoginRequest();
        req.setEmail("ghost@test.com");
        req.setPassword("AnyPass12!");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    // LOGOUT  (POST /auth/logout  -  LogoutController)

    @Test
    void logout_afterValidLogin_returns200AndExpiresBothCookies() throws Exception {
        // seed + login
        String email = "logout@test.com";
        String pass  = "LogoutP@ss1";
        userRepository.save(new User("logoutuser", email, encoder.encode(pass)));

        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail(email);
        loginReq.setPassword(pass);

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        Cookie accessCookie = loginResult.getResponse().getCookie("accessToken");

        // logout - send the cookie so JwtAuthFilter authenticates the request and LogoutController can revoke the refresh tokens
        mockMvc.perform(post("/auth/logout")
                        .cookie(accessCookie))
                .andExpect(status().isOk())
                // LogoutController body: Map.of("message","Logged out successfully")
                .andExpect(jsonPath("$.message").value("Logged out successfully"))
                // LogoutController sets maxAge=0 on both cookies
                .andExpect(cookie().maxAge("accessToken", 0))
                .andExpect(cookie().maxAge("refreshToken", 0));
    }
}