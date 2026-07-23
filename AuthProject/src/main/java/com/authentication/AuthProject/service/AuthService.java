package com.authentication.AuthProject.service;

import com.authentication.AuthProject.dto.request.LoginRequest;
import com.authentication.AuthProject.dto.request.SignupRequest;
import com.authentication.AuthProject.dto.response.AuthResponse;
import com.authentication.AuthProject.entity.User;
import com.authentication.AuthProject.exception.DuplicateResourceException;
import com.authentication.AuthProject.exception.InvalidCredentialsException;
import com.authentication.AuthProject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository repository;

    public AuthResponse signup(SignupRequest request) {

        if (repository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered.");
        }

        if (repository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicateResourceException("Phone number already registered.");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dob(request.getDob())
                .gender(request.getGender())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(request.getPassword())
                .build();

        User savedUser = repository.save(user);

        return AuthResponse.builder()
                .userId(savedUser.getId())
                .message("Signup Successful")
                .build();
    }

    public AuthResponse login(LoginRequest request) {

        User user = repository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new InvalidCredentialsException("Invalid email or password."));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password.");
        }

        return AuthResponse.builder()
                .userId(user.getId())
                .message("Login successful")
                .build();
    }
}
