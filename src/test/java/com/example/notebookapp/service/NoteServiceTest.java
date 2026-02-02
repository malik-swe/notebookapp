package com.example.notebookapp.service;

import com.example.notebookapp.exception.ForbiddenException;
import com.example.notebookapp.model.Note;
import com.example.notebookapp.model.Role;
import com.example.notebookapp.model.User;
import com.example.notebookapp.repository.NoteRepository;
import com.example.notebookapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private NoteService noteService;

    private User testUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        // create test users
        testUser = new User("testuser", "test@example.com", "hashedpass", Role.USER);
        setUserId(testUser, 1L);  // Use reflection to set ID

        otherUser = new User("otheruser", "other@example.com", "hashedpass", Role.USER);
        setUserId(otherUser, 2L);

        // set up security context
        SecurityContextHolder.setContext(securityContext);
    }

    // set ID via reflection (simulates JPA)
    private void setUserId(User user, Long id) {
        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // mock authentication
    private void mockAuthentication(User user) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                user.getEmail(), null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    @Test
    void getById_WhenUserOwnsNote_ShouldReturnNote() {

        mockAuthentication(testUser);  // login as testUser
        Long noteId = 1L;
        Note note = new Note("My Note", "My Content", testUser.getId());
        when(noteRepository.findById(noteId)).thenReturn(Optional.of(note));

        Note result = noteService.getById(noteId);

        assertNotNull(result);
        assertEquals("My Note", result.getTitle());
    }

    @Test
    void getById_WhenUserDoesNotOwnNote_ShouldThrowForbiddenException() {

        mockAuthentication(testUser);  // login as testUser (ID=1)
        Long noteId = 1L;
        Note note = new Note("Other's Note", "Content", otherUser.getId());  // note owned by otherUser (ID=2)
        when(noteRepository.findById(noteId)).thenReturn(Optional.of(note));

        assertThrows(ForbiddenException.class, () -> noteService.getById(noteId));
    }
}