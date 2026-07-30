package com.authentication.AuthProject.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            ResourceNotFoundException ex) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicate(
            DuplicateResourceException ex) {

        return buildResponse(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleCredentials(
            InvalidCredentialsException ex) {

        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(
            BadRequestException ex) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(UnverifiedUserException.class)
    public ResponseEntity<ApiErrorResponse> handleUnverifiedUser(
            UnverifiedUserException ex) {

        return buildResponse(
                HttpStatus.FORBIDDEN,
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                errors
        );
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleRateLimitExceeded(
            RateLimitExceededException ex) {

        log.warn("Rate limit exceeded: {}", ex.getMessage());

        return buildResponse(
                HttpStatus.TOO_MANY_REQUESTS,
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGlobalException(
            Exception ex) {

        log.error("Unexpected error occurred", ex);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Something went wrong",
                null
        );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            Map<String, String> fieldErrors) {

        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.status(status).body(response);
    }
}