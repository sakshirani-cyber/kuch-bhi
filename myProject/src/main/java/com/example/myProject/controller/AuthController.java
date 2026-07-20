package com.example.myProject.controller;

import com.example.myProject.dto.UserResponse;
import com.example.myProject.dto.loginRequest;
import com.example.myProject.dto.registerRequest;
import com.example.myProject.dto.updatePassword;
import com.example.myProject.dto.updateUsername;
import com.example.myProject.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody registerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody loginRequest request) {
        return ResponseEntity.ok(service.login(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(service.getUser(id));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<UserResponse> updatePassword(
            @PathVariable Long id,
            @Valid @RequestBody updatePassword request
    ) {
        return ResponseEntity.ok(service.updatePassword(id, request));
    }

    @PutMapping("/{id}/username")
    public ResponseEntity<UserResponse> updateUsername(
            @PathVariable Long id,
            @Valid @RequestBody updateUsername request
    ) {
        return ResponseEntity.ok(service.updateUsername(id, request));
    }
}
