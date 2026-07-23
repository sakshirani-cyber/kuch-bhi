package com.example.myProject.controller;

import com.example.myProject.dto.ApiResponse;
import com.example.myProject.dto.DeleteUserRequest;
import com.example.myProject.dto.LoginRequest;
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
import org.springframework.web.bind.annotation.DeleteMapping;
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
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("New User Registration processing with email={}", request.getEmail());
        try {
            ApiResponse<Void> body = service.register(request);
            return ResponseEntity.status(body.getStatus()).body(body);
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("unexpected error email={}", request.getEmail(), ex);
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Registration failed",
                    Map.of("error", "Unable to register user. Please try again.")
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login Attempted for email={}", request.getEmail());
        try {
            ApiResponse<UserResponse> body = service.login(request);
            return ResponseEntity.status(body.getStatus()).body(body);
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("unexpected error email={}", request.getEmail(), ex);
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Login failed",
                    Map.of("error", "Unable to login. Please try again.")
            );
        }
    }

    @GetMapping("/user/{email:.+}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(@PathVariable("email") String email) {
        log.info("Fetching User with email Id={}", email);
        try {
            ApiResponse<UserResponse> body = service.getUser(email);
            return ResponseEntity.status(body.getStatus()).body(body);
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("unexpected error in fetching user - {}", email, ex);
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to fetch user",
                    Map.of("error", "Unable to fetch user details. Please try again.")
            );
        }
    }

    @PutMapping("/update-password/{email:.+}")
    public ResponseEntity<ApiResponse<Void>> updatePasswordByEmail(
            @PathVariable("email") String email,
            @Valid @RequestBody UpdatePasswordRequest request
    ) {
        log.info("Password Update Initiated for {}", email);
        try {
            ApiResponse<Void> body = service.updatePassword(email, request);
            return ResponseEntity.status(body.getStatus()).body(body);
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("{} password unexpected error", email, ex);
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Password update failed",
                    Map.of("error", "Unable to update password. Please try again.")
            );
        }
    }

    @PutMapping("/update-username/{email:.+}")
    public ResponseEntity<ApiResponse<Void>> updateUsernameByEmail(
            @PathVariable("email") String email,
            @Valid @RequestBody UpdateUsernameRequest request
    ) {
        log.info("Username update initiated for {}", email);
        try {
            ApiResponse<Void> body = service.updateUsername(email, request);
            return ResponseEntity.status(body.getStatus()).body(body);
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected username update error for {}", email, ex);
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Username update failed",
                    Map.of("error", "Unable to update username. Please try again.")
            );
        }
    }

    @DeleteMapping("/delete-user/{email:.+}")
    public ResponseEntity<ApiResponse<Void>> deleteUserByEmail(
            @PathVariable("email") String email,
            @Valid @RequestBody DeleteUserRequest request
    ) {
        log.info("User deletion initiated for {}", email);
        try {
            ApiResponse<Void> body = service.deleteUser(email, request);
            return ResponseEntity.status(body.getStatus()).body(body);
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected user deletion error for {}", email, ex);
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "User deletion failed",
                    Map.of("error", "Unable to delete user. Please try again.")
            );
        }
    }
}
