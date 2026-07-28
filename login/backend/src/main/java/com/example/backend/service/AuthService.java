package com.example.backend.service;

// service to handle user authentication (login, signup, OTP verification, send/resend OTP)

import com.example.backend.dto.request.LoginRequest;
import com.example.backend.dto.request.ResendOtpRequest;
import com.example.backend.dto.request.SendOtpRequest;
import com.example.backend.dto.request.SignupRequest;
import com.example.backend.dto.request.VerifyOtpRequest;
import com.example.backend.dto.response.AuthResponse;
import com.example.backend.entity.User;
import com.example.backend.exception.InvalidCredentialsException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.exception.UserAlreadyExistsException;
import com.example.backend.mapper.UserMapper;
import com.example.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordService passwordService;
    private final EncryptionService encryptionService;
    private final UserMapper userMapper;
    private final JwtTokenService jwtTokenService;
    private final OtpService otpService;
    private final EmailNotificationService emailNotificationService;
    private final CacheManager cacheManager;

    public AuthService(UserRepository userRepository,
                       UserService userService,
                       PasswordService passwordService,
                       EncryptionService encryptionService,
                       UserMapper userMapper,
                       JwtTokenService jwtTokenService,
                       OtpService otpService,
                       EmailNotificationService emailNotificationService,
                       CacheManager cacheManager) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordService = passwordService;
        this.encryptionService = encryptionService;
        this.userMapper = userMapper;
        this.jwtTokenService = jwtTokenService;
        this.otpService = otpService;
        this.emailNotificationService = emailNotificationService;
        this.cacheManager = cacheManager;
    }

    public AuthResponse sendOtp(SendOtpRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Send OTP request cannot be null");
        }

        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.findByEmail(email).isPresent()) {
            log.error("Email already registered: {}", email);
            throw new UserAlreadyExistsException("Email is already registered");
        }

        String otp = otpService.generateAndStoreOtp(email);
        emailNotificationService.sendOtp(email, otp);

        return new AuthResponse(true, "OTP sent successfully to email", null, null);
    }

    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Verify OTP request cannot be null");
        }

        String email = request.getEmail().trim().toLowerCase();
        String inputOtp = request.getOtp().trim();

        if (otpService.isOtpExpired(email)) {
            throw new InvalidCredentialsException("OTP has expired. Please click Resend OTP.");
        }

        if (otpService.isMaxAttemptsExceeded(email)) {
            throw new InvalidCredentialsException("Maximum OTP verification attempts exceeded (3/3). Please request a new OTP.");
        }

        boolean isValid = otpService.validateOtp(email, inputOtp);
        if (!isValid) {
            int remaining = otpService.getRemainingAttempts(email);
            throw new InvalidCredentialsException("Invalid OTP. Remaining attempts: " + remaining);
        }

        otpService.markEmailAsVerified(email);
        log.info("Email verified successfully before signup for {}", email);

        return new AuthResponse(true, "Email verified successfully! You can now create your account.", null, null);
    }

    public AuthResponse resendOtp(ResendOtpRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Resend OTP request cannot be null");
        }

        String email = request.getEmail().trim().toLowerCase();

        String newOtp = otpService.generateAndStoreOtp(email);
        emailNotificationService.sendOtp(email, newOtp);

        return new AuthResponse(true, "OTP resent successfully. Please check your console/email.", null, null);
    }

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Signup request cannot be null");
        }

        String username = request.getUsername() != null ? request.getUsername().trim() : null;
        String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : null;

        log.info("Attempting signup for email: {}", email);

        if (email == null || !otpService.isEmailVerified(email)) {
            log.error("Signup rejected: Email {} has not been verified via OTP", email);
            throw new InvalidCredentialsException("EMAIL_NOT_VERIFIED: Please verify your email via OTP before creating your account.");
        }

        if (username != null && userRepository.findByUsername(username).isPresent()) {
            log.error("Username already exists");
            throw new UserAlreadyExistsException("Username already exists");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            log.error("Email already exists");
            throw new UserAlreadyExistsException("Email already exists");
        }
        if (request.getConfirmPassword() == null || !request.getPassword().equals(request.getConfirmPassword())) {
            log.error("Password and confirm password do not match");
            throw new IllegalArgumentException("Password and confirm password do not match");
        }

        String hashedPassword = passwordService.hashPassword(request.getPassword());
        String rawContactNumber = request.getContactNumber() != null ? request.getContactNumber().trim() : null;
        String encryptedContactNumber = encryptionService.encrypt(rawContactNumber);

        User user = Objects.requireNonNull(userMapper.toEntity(request, hashedPassword, encryptedContactNumber), 
            "Mapped user entity cannot be null"
        );
        user.setVerified(true);

        User savedUser = userRepository.save(user);
        otpService.clearEmailVerification(email);
        log.info("Signup successful for verified user: {}", savedUser.getUsername());

        String token = jwtTokenService.generateToken(savedUser.getUsername());
        return new AuthResponse(true, "Account created successfully", token, userMapper.toDto(savedUser));
    }

    public AuthResponse login(LoginRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Login request cannot be null");
        }
        
        String identifier = request.getIdentifier();
        String password = request.getPassword();

        log.info("Attempting login for identifier: {}", identifier);

        Cache usersCache = cacheManager.getCache("users");
        if (usersCache != null && usersCache.get(identifier) != null) {
            log.info("Cache HIT for user identifier: {}", identifier);
        } else {
            log.info("Cache MISS for user identifier: {}", identifier);
        }

        User user;
        try {
            user = userService.findByIdentifierCached(identifier);
        } catch (ResourceNotFoundException e) {
            log.error("Authentication failed during login: user not found");
            throw new InvalidCredentialsException("Invalid Credentials");
        }

        if (!passwordService.matches(password, user.getPassword())) {
            log.error("Authentication failed during login: incorrect password");
            throw new InvalidCredentialsException("Invalid Credentials");
        }

        if (!user.isVerified()) {
            log.warn("Login rejected: user email is unverified");
            throw new InvalidCredentialsException("ACCOUNT_NOT_VERIFIED: Account is not verified. Please verify your email via OTP.");
        }

        log.info("Login successful");
        String token = jwtTokenService.generateToken(user.getUsername());
        return new AuthResponse(true, "Hi there", token, userMapper.toDto(user));
    }
}
