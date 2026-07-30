package com.example.AuthProject.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.Map;

@Getter
public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final Map<String, String> errors;

    public ApiException(HttpStatus status, String message, Map<String, String> errors) {
        super(message);
        this.status = status;
        this.errors = errors != null ? errors : Collections.emptyMap();
    }
}
