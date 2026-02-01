package com.example.notebookapp.exception;

public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String resourceType, Long id) {
        super(String.format("You don't have permission to access this %s", resourceType));
    }
}