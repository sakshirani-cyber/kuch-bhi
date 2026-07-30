package com.example.AuthProject.controller;

import com.example.AuthProject.dto.ApiResponse;
import com.example.AuthProject.dto.AuthResponse;
import com.example.AuthProject.dto.DeleteUserRequest;
import com.example.AuthProject.dto.LoginRequest;
import com.example.AuthProject.dto.RegisterRequest;
import com.example.AuthProject.dto.ResendOtpRequest;
import com.example.AuthProject.dto.UpdatePasswordRequest;
import com.example.AuthProject.dto.UpdateUsernameRequest;
import com.example.AuthProject.dto.UserResponse;
import com.example.AuthProject.dto.VerifyOtpRequest;
import com.example.AuthProject.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("New User Registration processing with email={}", request.getEmail());
        ApiResponse<Void> body = service.register(request);
        return ResponseEntity.status(body.getStatus()).body(body);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        log.info("OTP verification requested for email={}", request.getEmail());
        ApiResponse<Void> body = service.verifyOtp(request);
        return ResponseEntity.status(body.getStatus()).body(body);
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        log.info("OTP resend requested for email={}", request.getEmail());
        ApiResponse<Void> body = service.resendOtp(request);
        return ResponseEntity.status(body.getStatus()).body(body);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login Attempted for email={}", request.getEmail());
        ApiResponse<AuthResponse> body = service.login(request);
        return ResponseEntity.status(body.getStatus()).body(body);
    }

    @GetMapping("/user/{email:.+}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(@PathVariable("email") String email) {
        log.info("Fetching User with email Id={}", email);
        ApiResponse<UserResponse> body = service.getUser(email);
        return ResponseEntity.status(body.getStatus()).body(body);
    }

    @PutMapping("/update-password/{email:.+}")
    public ResponseEntity<ApiResponse<Void>> updatePasswordByEmail(
            @PathVariable("email") String email,
            @Valid @RequestBody UpdatePasswordRequest request
    ) {
        log.info("Password Update Initiated for {}", email);
        ApiResponse<Void> body = service.updatePassword(email, request);
        return ResponseEntity.status(body.getStatus()).body(body);
    }

    @PutMapping("/update-username/{email:.+}")
    public ResponseEntity<ApiResponse<Void>> updateUsernameByEmail(
            @PathVariable("email") String email,
            @Valid @RequestBody UpdateUsernameRequest request
    ) {
        log.info("Username update initiated for {}", email);
        ApiResponse<Void> body = service.updateUsername(email, request);
        return ResponseEntity.status(body.getStatus()).body(body);
    }

    @DeleteMapping("/delete-user/{email:.+}")
    public ResponseEntity<ApiResponse<Void>> deleteUserByEmail(
            @PathVariable("email") String email,
            @Valid @RequestBody DeleteUserRequest request
    ) {
        log.info("User deletion initiated for {}", email);
        ApiResponse<Void> body = service.deleteUser(email, request);
        return ResponseEntity.status(body.getStatus()).body(body);
    }
}
