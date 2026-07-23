package com.preeti.authenticationdemo.controller;

import com.preeti.authenticationdemo.dto.ApiResponse;
import com.preeti.authenticationdemo.dto.DeleteUserRequest;
import com.preeti.authenticationdemo.dto.LoginRequest;
import com.preeti.authenticationdemo.dto.SignupRequest;
import com.preeti.authenticationdemo.dto.UpdateEmailRequest;
import com.preeti.authenticationdemo.dto.UpdatePasswordRequest;
import com.preeti.authenticationdemo.dto.UpdatePhoneNumberRequest;
import com.preeti.authenticationdemo.dto.UpdateUsernameRequest;
import com.preeti.authenticationdemo.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<String>> signup(@Valid @RequestBody SignupRequest request) {
        String result = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@Valid @RequestBody LoginRequest request) {
        String result = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PutMapping("/update/username")
    public ResponseEntity<ApiResponse<String>> updateUsername(@Valid @RequestBody UpdateUsernameRequest request) {
        String result = authService.updateUsername(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PutMapping("/update/password")
    public ResponseEntity<ApiResponse<String>> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        String result = authService.updatePassword(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PutMapping("/update/email")
    public ResponseEntity<ApiResponse<String>> updateEmail(@Valid @RequestBody UpdateEmailRequest request) {
        String result = authService.updateEmail(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PutMapping("/update/phone")
    public ResponseEntity<ApiResponse<String>> updatePhoneNumber(@Valid @RequestBody UpdatePhoneNumberRequest request) {
        String result = authService.updatePhoneNumber(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<String>> deleteUser(@Valid @RequestBody DeleteUserRequest request) {
        String result = authService.deleteUser(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

}
