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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final EncryptionService encryptionService;
    private final PhoneHashService phoneHashService;

    public AuthResponse signup(SignupRequest request) {
        log.debug("Checking duplicate email registration for: {}", request.getEmail());
        if (repository.existsByEmail(request.getEmail())) {
            log.warn("Signup failed: Email {} is already registered.", request.getEmail());
            throw new DuplicateResourceException("Email already registered.");
        }

        log.debug("Hashing and checking phone number duplicate registration.");
        String phoneHash = phoneHashService.hash(request.getPhoneNumber());

        if (repository.existsByPhoneNumberHash(phoneHash)) {
            log.warn("Signup failed: Phone number is already registered.");
            throw new DuplicateResourceException("Phone number already registered.");
        }

        log.debug("Building and saving new User entity.");
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
        log.info("New user signed up successfully with ID: {}", savedUser.getId());

        return AuthResponse.builder()
                .userId(savedUser.getId())
                .message("Signup Successful")
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        log.debug("Looking up user for login: {}", request.getEmail());
        User user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: User not found with email: {}", request.getEmail());
                    return new InvalidCredentialsException("Invalid email or password.");
                });

        log.debug("Verifying password credentials for: {}", request.getEmail());
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed: Password mismatch for email: {}", request.getEmail());
            throw new InvalidCredentialsException("Invalid email or password.");
        }

        log.info("User login validated successfully for ID: {}", user.getId());
        return AuthResponse.builder()
                .userId(user.getId())
                .message("Login successful")
                .build();
    }
}
