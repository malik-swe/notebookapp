package com.example.notebookapp.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;

public class PasswordPolicyValidator implements ConstraintValidator<ValidPassword, String> {

    // Common weak passwords to reject
    private static final List<String> COMMON_PASSWORDS = Arrays.asList(
            "password", "123456", "password123", "admin", "letmein",
            "welcome", "monkey", "dragon", "master", "sunshine",
            "qwerty", "abc123", "111111", "password1", "1234567890"
    );

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }

        context.disableDefaultConstraintViolation();

        // Minimum length: 12 characters (OWASP recommendation)
        if (password.length() < 12) {
            context.buildConstraintViolationWithTemplate(
                    "Password must be at least 12 characters long"
            ).addConstraintViolation();
            return false;
        }

        // Maximum length to prevent DoS
        if (password.length() > 128) {
            context.buildConstraintViolationWithTemplate(
                    "Password must not exceed 128 characters"
            ).addConstraintViolation();
            return false;
        }

        // Must contain at least one uppercase letter
        if (!password.matches(".*[A-Z].*")) {
            context.buildConstraintViolationWithTemplate(
                    "Password must contain at least one uppercase letter"
            ).addConstraintViolation();
            return false;
        }

        // Must contain at least one lowercase letter
        if (!password.matches(".*[a-z].*")) {
            context.buildConstraintViolationWithTemplate(
                    "Password must contain at least one lowercase letter"
            ).addConstraintViolation();
            return false;
        }

        // Must contain at least one digit
        if (!password.matches(".*\\d.*")) {
            context.buildConstraintViolationWithTemplate(
                    "Password must contain at least one digit"
            ).addConstraintViolation();
            return false;
        }

        // Must contain at least one special character
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            context.buildConstraintViolationWithTemplate(
                    "Password must contain at least one special character"
            ).addConstraintViolation();
            return false;
        }

        // Check against common passwords (case-insensitive)
        if (COMMON_PASSWORDS.contains(password.toLowerCase())) {
            context.buildConstraintViolationWithTemplate(
                    "Password is too common. Please choose a stronger password"
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}