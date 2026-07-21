package com.preeti.authenticationdemo.controller;

import com.preeti.authenticationdemo.dto.LoginRequest;
import com.preeti.authenticationdemo.dto.SignupRequest;
import com.preeti.authenticationdemo.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public String signup(@Valid @RequestBody SignupRequest request) {

        return authService.signup(request);
    }

}