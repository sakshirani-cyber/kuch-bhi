package com.authentication.AuthProject.service;

import com.authentication.AuthProject.dto.request.LoginRequest;
import com.authentication.AuthProject.dto.request.ResendOtpRequest;
import com.authentication.AuthProject.dto.request.SignupRequest;
import com.authentication.AuthProject.dto.request.VerifyOtpRequest;
import com.authentication.AuthProject.dto.response.AuthResponse;
import com.authentication.AuthProject.entity.User;
import com.authentication.AuthProject.exception.DuplicateResourceException;
import com.authentication.AuthProject.exception.InvalidCredentialsException;
import com.authentication.AuthProject.exception.UnverifiedUserException;
import com.authentication.AuthProject.repository.UserRepository;
import com.authentication.AuthProject.security.JwtService;
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
    private final OtpService otpService;
    private final UserService userService;
    private final RateLimitService rateLimitService;
    private final JwtService jwtService;

    public AuthResponse signup(SignupRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        log.debug("Checking duplicate email registration for: {}", email);
        if (repository.existsByEmail(email)) {
            log.warn("Signup failed: Email {} is already registered.", email);
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
                .email(email)
                .phoneNumber(encryptionService.encrypt(request.getPhoneNumber()))
                .phoneNumberHash(phoneHash)
                .password(passwordEncoder.encode(request.getPassword()))
                .verified(false)
                .build();

        User savedUser = repository.save(user);
        log.info("New user signed up successfully with ID: {}", savedUser.getId());

        otpService.issueOtpForSignup(savedUser.getEmail());

        return AuthResponse.builder()
                .userId(savedUser.getId())
                .message("Signup successful. Verify OTP sent to your email.")
                .build();
    }


    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        log.debug("Looking up user for login: {}", email);
        User user = repository.findByEmail(email)
                .orElseThrow(() -> {

                    rateLimitService.recordFailedAttempt(email);

                    log.warn("Login failed: User not found with email: {}", email);
                    return new InvalidCredentialsException("Invalid email or password.");
                });

        // Check if user is already blocked
        rateLimitService.checkLoginBlocked(email);

        log.debug("Verifying password credentials for: {}", email);

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {

            // Record failed attempt
            rateLimitService.recordFailedAttempt(email);

            log.warn("Login failed: Password mismatch for email: {}", email);
            throw new InvalidCredentialsException("Invalid email or password.");
        }

        // Password is correct then reset failed attempts
        rateLimitService.resetLoginLimit(email);

        if (!user.isVerified()) {
            log.warn("Login failed: Email not verified for: {}", email);
            throw new UnverifiedUserException("Email not verified. Please verify OTP.");
        }

        userService.warmProfileCache(user);

        log.info("User login validated successfully for ID: {}", user.getId());

        String token = jwtService.generateToken(user.getEmail());

        return AuthResponse.builder()
                .userId(user.getId())
                .message("Login successful")
                .token(token)
                .build();
    }

    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        log.debug("Verifying OTP for email: {}", request.getEmail());
        otpService.verifyOtp(request.getEmail(), request.getOtp());
        return AuthResponse.builder()
                .message("Email verified successfully.")
                .build();
    }

    public AuthResponse resendOtp(ResendOtpRequest request) {
        log.debug("Resending OTP for email: {}", request.getEmail());
        otpService.resendOtp(request.getEmail());
        return AuthResponse.builder()
                .message("OTP sent to your email.")
                .build();
    }
}
