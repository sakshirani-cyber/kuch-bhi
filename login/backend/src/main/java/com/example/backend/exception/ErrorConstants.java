package com.example.backend.exception;

// Constants for standard error messages used across the application

public final class ErrorConstants {
    private ErrorConstants() {
        // Restrict instantiation
    }

    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    public static final String INVALID_PASSWORD = "INVALID_PASSWORD";
    public static final String USERNAME_ALREADY_EXISTS = "Username already exists";
    public static final String EMAIL_ALREADY_EXISTS = "Email already exists";
    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
}
