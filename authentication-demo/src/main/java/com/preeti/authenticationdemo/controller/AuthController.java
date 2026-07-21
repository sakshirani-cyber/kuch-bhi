package com.preeti.authenticationdemo.controller;

import com.preeti.authenticationdemo.dto.LoginRequest;
import com.preeti.authenticationdemo.dto.SignupRequest;
import com.preeti.authenticationdemo.dto.UpdateRequest;
import com.preeti.authenticationdemo.exception.InvalidCredentialsException;
import com.preeti.authenticationdemo.exception.UserAlreadyExistsException;
import com.preeti.authenticationdemo.exception.UserNotFoundException;
import com.preeti.authenticationdemo.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        try {
            String result = authService.signup(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (UserAlreadyExistsException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong during signup. Please try again.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request) {
        try {
            String result = authService.login(request);
            return ResponseEntity.ok(result);
        } catch (UserNotFoundException | InvalidCredentialsException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exception.getMessage());
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong during login. Please try again.");
        }
    }

    @PutMapping("/update")
    public ResponseEntity<String> update(@Valid @RequestBody UpdateRequest request) {
        try {
            String result = authService.updateCredentials(request);
            return ResponseEntity.ok(result);
        } catch (UserNotFoundException | InvalidCredentialsException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exception.getMessage());
        } catch (UserAlreadyExistsException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong while updating your profile. Please try again.");
        }
    }

}
