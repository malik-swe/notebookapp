package com.example.notebookapp.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PasswordValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    // hold password for validation
    private static class TestDto {
        @ValidPassword
        private String password;

        public TestDto(String password) {
            this.password = password;
        }
    }

    @Test
    void validatePassword_WithValidPassword_ShouldPass() {

        TestDto dto = new TestDto("ValidPass123!");

        Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());  // no violations == valid password
    }

    @Test
    void validatePassword_TooShort_ShouldFail() {
        TestDto dto = new TestDto("Short1!");

        Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        String message = violations.iterator().next().getMessage();
        assertTrue(message.contains("at least 12 characters"));
    }
}