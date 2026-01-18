package com.example.notebookapp.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UsernameValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUsername {

    String message() default
            "Username may contain only letters, numbers, and underscores";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
