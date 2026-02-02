package com.example.notebookapp.integration;

import com.example.notebookapp.model.Role;
import com.example.notebookapp.repository.NoteRepository;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import tools.jackson.databind.ObjectMapper;
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

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecuredEndpointsIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private NoteRepository noteRepository;
    @Autowired private RefreshTokenRepository   refreshTokenRepository;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    private static final String USER_A_PW = "UserAPass1!";
    private static final String USER_B_PW = "UserBPass1!";
    private static final String ADMIN_PW  = "AdminPass1!";

    private User userA;
    private User userB;
    private User admin;

    @BeforeEach
    void seed() {
        refreshTokenRepository.deleteAll();
        noteRepository.deleteAll();
        userRepository.deleteAll();

        userA = userRepository.save(
                new User("usera", "usera@test.com", encoder.encode(USER_A_PW), Role.USER));
        userB = userRepository.save(
                new User("userb", "userb@test.com", encoder.encode(USER_B_PW), Role.USER));
        admin = userRepository.save(
                new User("admin", "admin@test.com", encoder.encode(ADMIN_PW),  Role.ADMIN));
    }

    // helper logs in via AuthController, returns the accessToken cookie
    private Cookie loginAndGetCookie(String email, String password) throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail(email);
        req.setPassword(password);

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        Cookie c = result.getResponse().getCookie("accessToken");
        if (c == null) {
            throw new AssertionError("accessToken cookie missing after login for " + email);
        }
        return c;
    }

    @Test
    void notes_create_returns201WithNoteFields() throws Exception {
        Cookie cookie = loginAndGetCookie(userA.getEmail(), USER_A_PW);

        // Map because CreateNoteRequest has no setters
        Map<String, String> payload = Map.of("title", "First Note", "content", "Hello");

        mockMvc.perform(post("/notes")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                // NoteController returns Note entity: id, title, content, userId
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("First Note"))
                .andExpect(jsonPath("$.content").value("Hello"))
                .andExpect(jsonPath("$.userId").value(userA.getId()));
    }

    @Test
    void notes_getById_ownNote_returns200() throws Exception {
        Cookie cookie = loginAndGetCookie(userA.getEmail(), USER_A_PW);

        // create
        MvcResult createRes = mockMvc.perform(post("/notes")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("title", "ReadMe", "content", "body"))))
                .andExpect(status().isCreated())
                .andReturn();

        long noteId = objectMapper.readTree(
                createRes.getResponse().getContentAsString()).get("id").asLong();

        // read back
        mockMvc.perform(get("/notes/" + noteId).cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(noteId))
                .andExpect(jsonPath("$.title").value("ReadMe"));
    }

    @Test
    void notes_getAll_returnsOnlyOwnNotes() throws Exception {
        Cookie cookieA = loginAndGetCookie(userA.getEmail(), USER_A_PW);
        Cookie cookieB = loginAndGetCookie(userB.getEmail(), USER_B_PW);

        // A creates 2
        mockMvc.perform(post("/notes").cookie(cookieA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("title", "A1", "content", "a1"))))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/notes").cookie(cookieA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("title", "A2", "content", "a2"))))
                .andExpect(status().isCreated());

        // B creates 1
        mockMvc.perform(post("/notes").cookie(cookieB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("title", "B1", "content", "b1"))))
                .andExpect(status().isCreated());

        // A sees 2
        mockMvc.perform(get("/notes").cookie(cookieA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        // B sees 1
        mockMvc.perform(get("/notes").cookie(cookieB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void notes_delete_ownNote_returns204AndNoteIsGone() throws Exception {
        Cookie cookie = loginAndGetCookie(userA.getEmail(), USER_A_PW);

        MvcResult createRes = mockMvc.perform(post("/notes")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("title", "Bye", "content", "gone soon"))))
                .andExpect(status().isCreated())
                .andReturn();

        long noteId = objectMapper.readTree(
                createRes.getResponse().getContentAsString()).get("id").asLong();

        // NoteController.delete -> ResponseEntity.noContent() -> 204, empty body
        mockMvc.perform(delete("/notes/" + noteId).cookie(cookie))
                .andExpect(status().isNoContent());

        // confirm gone - ResourceNotFoundException -> 404
        mockMvc.perform(get("/notes/" + noteId).cookie(cookie))
                .andExpect(status().isNotFound());
    }

    // DATA ISOLATION  -  user B cannot read or delete user A's notes
    @Test
    void notes_userB_cannotReadUserANote_returns403() throws Exception {
        Cookie cookieA = loginAndGetCookie(userA.getEmail(), USER_A_PW);
        Cookie cookieB = loginAndGetCookie(userB.getEmail(), USER_B_PW);

        // A creates
        MvcResult createRes = mockMvc.perform(post("/notes")
                        .cookie(cookieA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("title", "Secret", "content", "top secret"))))
                .andExpect(status().isCreated())
                .andReturn();

        long noteId = objectMapper.readTree(
                createRes.getResponse().getContentAsString()).get("id").asLong();

        // B tries to read -> NoteService throws ForbiddenException -> GlobalExceptionHandler:
        //   ErrorResponse { status:403, error:"Forbidden",
        //                   message:"You don't have permission to access this resource" }
        mockMvc.perform(get("/notes/" + noteId).cookie(cookieB))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value(
                        "You don't have permission to access this resource"));
    }

    @Test
    void notes_userB_cannotDeleteUserANote_returns403_noteStillExists() throws Exception {
        Cookie cookieA = loginAndGetCookie(userA.getEmail(), USER_A_PW);
        Cookie cookieB = loginAndGetCookie(userB.getEmail(), USER_B_PW);

        // A creates
        MvcResult createRes = mockMvc.perform(post("/notes")
                        .cookie(cookieA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("title", "Protected", "content", "keep"))))
                .andExpect(status().isCreated())
                .andReturn();

        long noteId = objectMapper.readTree(
                createRes.getResponse().getContentAsString()).get("id").asLong();

        // B tries to delete -> NoteService.delete calls getById which throws ForbiddenException
        mockMvc.perform(delete("/notes/" + noteId).cookie(cookieB))
                .andExpect(status().isForbidden());

        // A can still read it - note was not deleted
        mockMvc.perform(get("/notes/" + noteId).cookie(cookieA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Protected"));
    }

    @Test
    void notes_nonExistentId_returns404() throws Exception {
        Cookie cookie = loginAndGetCookie(userA.getEmail(), USER_A_PW);

        // NoteService.getById -> noteRepository.findById returns empty
        //   -> ResourceNotFoundException("Note", 999999)
        //   -> GlobalExceptionHandler -> ErrorResponse { status:404, error:"Not Found" }
        mockMvc.perform(get("/notes/999999").cookie(cookie))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // ROLE-BASED ACCESS  -  /admin endpoints

    @Test
    void admin_stats_adminCanAccess() throws Exception {
        Cookie cookie = loginAndGetCookie(admin.getEmail(), ADMIN_PW);

        // AdminController.getStatistics returns:
        //   Map.of("totalUsers", count, "message", "Only admins can see this!")
        mockMvc.perform(get("/admin/stats").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").isNumber())
                .andExpect(jsonPath("$.message").value("Only admins can see this!"));
    }

    @Test
    void admin_stats_regularUserForbidden() throws Exception {
        Cookie cookie = loginAndGetCookie(userA.getEmail(), USER_A_PW);

        // @PreAuthorize("hasRole('ADMIN')") -> AccessDeniedException -> 403
        mockMvc.perform(get("/admin/stats").cookie(cookie))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_users_adminCanAccess() throws Exception {
        Cookie cookie = loginAndGetCookie(admin.getEmail(), ADMIN_PW);

        // AdminController.getAllUsers returns List<User> - we seeded 3
        mockMvc.perform(get("/admin/users").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void admin_users_regularUserForbidden() throws Exception {
        Cookie cookie = loginAndGetCookie(userA.getEmail(), USER_A_PW);

        mockMvc.perform(get("/admin/users").cookie(cookie))
                .andExpect(status().isForbidden());
    }
}