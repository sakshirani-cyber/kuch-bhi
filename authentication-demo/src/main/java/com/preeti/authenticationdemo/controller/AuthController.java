package com.preeti.authenticationdemo.controller;

import com.preeti.authenticationdemo.dto.DeleteUserRequest;
import com.preeti.authenticationdemo.dto.LoginRequest;
import com.preeti.authenticationdemo.dto.SignupRequest;
import com.preeti.authenticationdemo.dto.UpdateEmailRequest;
import com.preeti.authenticationdemo.dto.UpdatePasswordRequest;
import com.preeti.authenticationdemo.dto.UpdatePhoneNumberRequest;
import com.preeti.authenticationdemo.dto.UpdateUsernameRequest;
import com.preeti.authenticationdemo.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequest request) {
        String result = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request) {
        String result = authService.login(request);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/update/username")
    public ResponseEntity<String> updateUsername(@Valid @RequestBody UpdateUsernameRequest request) {
        return ResponseEntity.ok(authService.updateUsername(request));
    }

    @PutMapping("/update/password")
    public ResponseEntity<String> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        return ResponseEntity.ok(authService.updatePassword(request));
    }

    @PutMapping("/update/email")
    public ResponseEntity<String> updateEmail(@Valid @RequestBody UpdateEmailRequest request) {
        return ResponseEntity.ok(authService.updateEmail(request));
    }

    @PutMapping("/update/phone")
    public ResponseEntity<String> updatePhoneNumber(@Valid @RequestBody UpdatePhoneNumberRequest request) {
        return ResponseEntity.ok(authService.updatePhoneNumber(request));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteUser(@Valid @RequestBody DeleteUserRequest request) {
        return ResponseEntity.ok(authService.deleteUser(request));
    }

}
