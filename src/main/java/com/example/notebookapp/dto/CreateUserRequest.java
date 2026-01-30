package com.example.notebookapp.dto;

import com.example.notebookapp.validation.ValidPassword;
import com.example.notebookapp.validation.ValidUsername;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateUserRequest {

    @NotNull
    @Size(min = 3, max = 50)
    @ValidUsername
    private String username;

    @NotNull
    @Email
    @Size(max = 255)
    private String email;

    @NotNull
    @ValidPassword
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
