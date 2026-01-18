package com.example.notebookapp.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UsernameValidator
        implements ConstraintValidator<ValidUsername, String> {

    private static final String USERNAME_REGEX = "^[a-zA-Z0-9_]+$";

    @Override
    public boolean isValid(String value,
                           ConstraintValidatorContext context) {

        // null is handled by @NotNull
        if (value == null) {
            return true;
        }

        return value.matches(USERNAME_REGEX);
    }
}
