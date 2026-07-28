package com.example.backend.service;

// Service to handle user authentication (login and signup)

import com.example.backend.dto.request.LoginRequest;
import com.example.backend.dto.request.SignupRequest;
import com.example.backend.dto.response.AuthResponse;
import com.example.backend.entity.User;
import com.example.backend.exception.InvalidCredentialsException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.exception.UserAlreadyExistsException;
import com.example.backend.mapper.UserMapper;
import com.example.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
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

    public AuthService(UserRepository userRepository, UserService userService, PasswordService passwordService, EncryptionService encryptionService, UserMapper userMapper, JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordService = passwordService;
        this.encryptionService = encryptionService;
        this.userMapper = userMapper;
        this.jwtTokenService = jwtTokenService;
    }

    public AuthResponse login(LoginRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Login request cannot be null");
        }
        
        String identifier = request.getIdentifier();
        String password = request.getPassword();

        log.info("Attempting login");

        // Rely on UserService's cached method to find user
        User user;
		try {
			user = userService.findByIdentifierCached(identifier);
		} catch (ResourceNotFoundException e) {
			log.error("Authentication failed during login");
			throw new InvalidCredentialsException("Invalid Credentials");
		}

        if (!passwordService.matches(password, user.getPassword())) {
            log.error("Authentication failed during login");
            throw new InvalidCredentialsException("Invalid Credentials");
        }

        log.info("Login successful");
        String token = jwtTokenService.generateToken(user.getUsername());
        return new AuthResponse(true, "Hi there", token, userMapper.toDto(user));
    }

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Signup request cannot be null");
        }

        String username = request.getUsername() != null ? request.getUsername().trim() : null;
        String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : null;

        log.info("Attempting signup");

        if (username != null && userRepository.findByUsername(username).isPresent()) {
            log.error("Username already exists");
            throw new UserAlreadyExistsException("Username already exists");
        }
        if (email != null && userRepository.findByEmail(email).isPresent()) {
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

        User savedUser = userRepository.save(user);
        log.info("Signup successful");
        String token = jwtTokenService.generateToken(savedUser.getUsername());
        return new AuthResponse(true, "Account created successfully", token, userMapper.toDto(savedUser));
    }
}
