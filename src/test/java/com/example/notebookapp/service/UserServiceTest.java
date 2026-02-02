package com.example.notebookapp.service;

import com.example.notebookapp.model.User;
import com.example.notebookapp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_WithValidData_ShouldCreateUserAndHashPassword() {

        String username = "testuser";
        String email = "test@example.com";
        String plainPassword = "password123";

        // tell mock what to return when methods are called
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);  // return the user that was passed in
        });

        // call tested method
        User result = userService.createUser(username, email, plainPassword);

        // verify the results
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(email, result.getEmail());
        assertNotEquals(plainPassword, result.getPassword());  // Should be hashed!
        assertTrue(result.getPassword().startsWith("$2a$"));   // BCrypt hash

        // verify interactions with mock
        verify(userRepository, times(1)).findByEmail(email);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_WithDuplicateEmail_ShouldThrowException() {

        String email = "existing@example.com";
        User existingUser = new User("existing", email, "hashedpass");
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        // verify exception is thrown
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser("newuser", email, "password123")
        );

        assertEquals("Email already in use", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));  // Should NOT save
    }


}