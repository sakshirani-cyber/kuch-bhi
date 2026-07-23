package com.authentication.AuthProject.service;

import com.authentication.AuthProject.dto.request.LoginRequest;
import com.authentication.AuthProject.dto.request.SignupRequest;
import com.authentication.AuthProject.dto.response.AuthResponse;
import com.authentication.AuthProject.entity.User;
import com.authentication.AuthProject.exception.DuplicateResourceException;
import com.authentication.AuthProject.exception.InvalidCredentialsException;
import com.authentication.AuthProject.repository.UserRepository;
import com.authentication.AuthProject.util.EncryptionService;
import com.authentication.AuthProject.util.PhoneHashService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final EncryptionService encryptionService;
    private final PhoneHashService phoneHashService;

    public AuthResponse signup(SignupRequest request) {

        if (repository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered.");
        }

        String phoneHash = phoneHashService.hash(request.getPhoneNumber());

        if (repository.existsByPhoneNumberHash(phoneHash)) {
            throw new DuplicateResourceException("Phone number already registered.");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dob(request.getDob())
                .gender(request.getGender())
                .email(request.getEmail())
                .phoneNumber(encryptionService.encrypt(request.getPhoneNumber()))
                .phoneNumberHash(phoneHash)
                .password(passwordEncoder.encode(request.getPassword()))
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

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password.");
        }

        return AuthResponse.builder()
                .userId(user.getId())
                .message("Login successful")
                .build();
    }
}
