package com.example.myProject.controller;

import com.example.myProject.dto.LoginRequest;
import com.example.myProject.dto.MessageResponse;
import com.example.myProject.dto.RegisterRequest;
import com.example.myProject.dto.UpdatePasswordRequest;
import com.example.myProject.dto.UpdateUsernameRequest;
import com.example.myProject.dto.UserResponse;
import com.example.myProject.exception.ApiException;
import com.example.myProject.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /api/v1/auth/register email={}", request.getEmail());
        try {
            MessageResponse response = service.register(request);
            log.info("POST /api/v1/auth/register completed email={}", request.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ApiException ex) {
            log.warn("POST /api/v1/auth/register failed email={} status={} message={}",
                    request.getEmail(), ex.getStatus(), ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("POST /api/v1/auth/register unexpected error email={}", request.getEmail(), ex);
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Registration failed",
                    Map.of("error", ex.getMessage())
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/v1/auth/login email={}", request.getEmail());
        try {
            UserResponse response = service.login(request);
            log.info("POST /api/v1/auth/login completed userId={}", response.getUserId());
            return ResponseEntity.ok(response);
        } catch (ApiException ex) {
            log.warn("POST /api/v1/auth/login failed email={} status={} message={}",
                    request.getEmail(), ex.getStatus(), ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("POST /api/v1/auth/login unexpected error email={}", request.getEmail(), ex);
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Login failed",
                    Map.of("error", ex.getMessage())
            );
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        log.info("GET /api/v1/auth/{}", id);
        try {
            UserResponse response = service.getUser(id);
            log.info("GET /api/v1/auth/{} completed", id);
            return ResponseEntity.ok(response);
        } catch (ApiException ex) {
            log.warn("GET /api/v1/auth/{} failed status={} message={}", id, ex.getStatus(), ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("GET /api/v1/auth/{} unexpected error", id, ex);
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to fetch user",
                    Map.of("error", ex.getMessage())
            );
        }
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<MessageResponse> updatePassword(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePasswordRequest request
    ) {
        log.info("PUT /api/v1/auth/{}/password", id);
        try {
            MessageResponse response = service.updatePassword(id, request);
            log.info("PUT /api/v1/auth/{}/password completed", id);
            return ResponseEntity.ok(response);
        } catch (ApiException ex) {
            log.warn("PUT /api/v1/auth/{}/password failed status={} message={}",
                    id, ex.getStatus(), ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("PUT /api/v1/auth/{}/password unexpected error", id, ex);
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Password update failed",
                    Map.of("error", ex.getMessage())
            );
        }
    }

    @PutMapping("/{id}/username")
    public ResponseEntity<MessageResponse> updateUsername(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUsernameRequest request
    ) {
        log.info("PUT /api/v1/auth/{}/username", id);
        try {
            MessageResponse response = service.updateUsername(id, request);
            log.info("PUT /api/v1/auth/{}/username completed", id);
            return ResponseEntity.ok(response);
        } catch (ApiException ex) {
            log.warn("PUT /api/v1/auth/{}/username failed status={} message={}",
                    id, ex.getStatus(), ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("PUT /api/v1/auth/{}/username unexpected error", id, ex);
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Username update failed",
                    Map.of("error", ex.getMessage())
            );
        }
    }
}
