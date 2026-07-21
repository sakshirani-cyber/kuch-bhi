package com.authentication.AuthProject.controller;

import com.authentication.AuthProject.dto.request.LoginRequest;
import com.authentication.AuthProject.dto.request.SignupRequest;
import com.authentication.AuthProject.dto.response.LoginResponse;
import com.authentication.AuthProject.dto.response.SignupResponse;
import com.authentication.AuthProject.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    @GetMapping
    public String greet() {
        return "Authentication API is Running";
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(
            @Valid @RequestBody SignupRequest request) {

        SignupResponse response = service.signup(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = service.login(request);

        return ResponseEntity.ok(response);
    }
}

