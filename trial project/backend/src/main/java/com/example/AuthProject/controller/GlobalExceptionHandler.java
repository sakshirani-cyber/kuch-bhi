package com.example.AuthProject.controller;

import com.example.AuthProject.dto.ApiResponse;
import com.example.AuthProject.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final DataSize maxFileSize;

    public GlobalExceptionHandler(
            @Value("${app.files.max-size:10MB}") DataSize maxFileSize
    ) {
        this.maxFileSize = maxFileSize;
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException ex) {
        log.warn("ApiException status={} message={} errors={}",
                ex.getStatus().value(), ex.getMessage(), ex.getErrors());
        return ResponseEntity
                .status(ex.getStatus())
                .body(ApiResponse.failure(ex.getStatus(), ex.getMessage(), ex.getErrors()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.failure(
                        HttpStatus.FORBIDDEN,
                        "Access denied",
                        Map.of("auth", "You do not have permission to perform this action")
                ));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.failure(
                        HttpStatus.UNAUTHORIZED,
                        "Authentication required",
                        Map.of("auth", "Valid JWT token is required")
                ));
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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadable(HttpMessageNotReadableException ex) {
        log.warn("Malformed request body: {}", ex.getMostSpecificCause().getMessage());
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.failure(
                        HttpStatus.BAD_REQUEST,
                        "Invalid request body",
                        Map.of("body", "Request body is missing or malformed")
                ));
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingPathVariable(MissingPathVariableException ex) {
        log.warn("Missing path variable: {}", ex.getVariableName());
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.failure(
                        HttpStatus.BAD_REQUEST,
                        "Missing path variable",
                        Map.of(ex.getVariableName(), "Path variable is required")
                ));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(MissingServletRequestParameterException ex) {
        log.warn("Missing request parameter: {}", ex.getParameterName());
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.failure(
                        HttpStatus.BAD_REQUEST,
                        "Missing request parameter",
                        Map.of(ex.getParameterName(), "Request parameter is required")
                ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String name = ex.getName() != null ? ex.getName() : "parameter";
        log.warn("Type mismatch for parameter={}", name);
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.failure(
                        HttpStatus.BAD_REQUEST,
                        "Invalid parameter type",
                        Map.of(name, "Invalid value for parameter")
                ));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.warn("Method not supported: {}", ex.getMethod());
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.failure(
                        HttpStatus.METHOD_NOT_ALLOWED,
                        "Method not allowed",
                        Map.of("method", "HTTP method is not supported for this endpoint")
                ));
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleNotFound(Exception ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(
                        HttpStatus.NOT_FOUND,
                        "Resource not found",
                        Map.of("path", "No endpoint found for this request")
                ));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleStatus(ResponseStatusException ex) {
        log.warn("ResponseStatusException status={} reason={}",
                ex.getStatusCode().value(), ex.getReason());
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(ApiResponse.failure(
                        status,
                        ex.getReason() != null ? ex.getReason() : "Request failed",
                        Collections.emptyMap()
                ));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        log.warn("Upload rejected: file exceeds max size {}", maxFileSize);
        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ApiResponse.failure(
                        HttpStatus.PAYLOAD_TOO_LARGE,
                        "File upload failed",
                        Map.of("file", maxFileSizeExceededMessage())
                ));
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiResponse<Void>> handleMultipart(MultipartException ex) {
        if (isMaxUploadSizeExceeded(ex)) {
            log.warn("Upload rejected (multipart): file exceeds max size {}", maxFileSize);
            return ResponseEntity
                    .status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body(ApiResponse.failure(
                            HttpStatus.PAYLOAD_TOO_LARGE,
                            "File upload failed",
                            Map.of("file", maxFileSizeExceededMessage())
                    ));
        }
        log.warn("Multipart request failed: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(
                        HttpStatus.BAD_REQUEST,
                        "File upload failed",
                        Map.of("file", "Could not read the uploaded file. Please try again.")
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

    private String maxFileSizeExceededMessage() {
        return "File exceeds the maximum allowed size of " + formatDataSize(maxFileSize);
    }

    private static boolean isMaxUploadSizeExceeded(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            if (current instanceof MaxUploadSizeExceededException) {
                return true;
            }
            String msg = current.getMessage();
            if (msg != null && msg.toLowerCase().contains("size")
                    && (msg.toLowerCase().contains("exceed") || msg.toLowerCase().contains("limit"))) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static String formatDataSize(DataSize size) {
        long bytes = size.toBytes();
        if (bytes % (1024L * 1024L) == 0) {
            return (bytes / (1024L * 1024L)) + " MB";
        }
        if (bytes % 1024L == 0) {
            return (bytes / 1024L) + " KB";
        }
        return bytes + " bytes";
    }
}
