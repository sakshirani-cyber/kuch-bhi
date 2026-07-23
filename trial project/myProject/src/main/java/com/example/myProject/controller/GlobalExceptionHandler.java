package com.example.myProject.controller;

import com.example.myProject.dto.ApiResponse;
import com.example.myProject.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException ex) {
        log.warn("ApiException status={} message={} errors={}",
                ex.getStatus().value(), ex.getMessage(), ex.getErrors());
        return ResponseEntity
                .status(ex.getStatus())
                .body(ApiResponse.failure(ex.getStatus(), ex.getMessage(), ex.getErrors()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        log.warn("Validation failed errors={}", fieldErrors);
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.failure(HttpStatus.BAD_REQUEST, "Validation failed", fieldErrors));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleStatus(ResponseStatusException ex) {
        log.warn("ResponseStatusException status={} reason={}",
                ex.getStatusCode().value(), ex.getReason());
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(ApiResponse.failure(
                        status,
                        ex.getReason() != null ? ex.getReason() : "Request failed",
                        Collections.emptyMap()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Internal server error",
                        Map.of("error", "Something went wrong. Please try again.")
                ));
    }
}
