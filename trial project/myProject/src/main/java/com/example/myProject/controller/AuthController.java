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
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.register(request));
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Registration failed",
                    Map.of("error", ex.getMessage())
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(service.login(request));
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Login failed",
                    Map.of("error", ex.getMessage())
            );
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.getUser(id));
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
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
        try {
            return ResponseEntity.ok(service.updatePassword(id, request));
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
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
        try {
            return ResponseEntity.ok(service.updateUsername(id, request));
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Username update failed",
                    Map.of("error", ex.getMessage())
            );
        }
    }
}
