package com.example.myProject.service;

import com.example.myProject.dto.LoginRequest;
import com.example.myProject.dto.MessageResponse;
import com.example.myProject.dto.RegisterRequest;
import com.example.myProject.dto.UpdatePasswordRequest;
import com.example.myProject.dto.UpdateUsernameRequest;
import com.example.myProject.dto.UserResponse;
import com.example.myProject.entity.User;
import com.example.myProject.exception.ApiException;
import com.example.myProject.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public MessageResponse register(RegisterRequest request) {
        log.info("Registration attempt for email={}", request.getEmail());
        try {
            if (repository.existsByEmail(request.getEmail())) {
                log.warn("Registration failed: email already exists email={}", request.getEmail());
                throw new ApiException(
                        HttpStatus.CONFLICT,
                        "Registration failed",
                        Map.of("email", "User already exists")
                );
            }

            int age = calculateAge(request.getDateOfBirth());
            String hashedPassword = passwordEncoder.encode(request.getPassword());

            repository.insertUser(
                    request.getUsername(),
                    request.getEmail(),
                    hashedPassword,
                    request.getContactNumber(),
                    request.getDateOfBirth(),
                    age
            );

            log.info("User registered successfully email={} age={}", request.getEmail(), age);
            return new MessageResponse("User registered successfully!");
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Registration failed for email={}", request.getEmail(), ex);
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Registration failed",
                    Map.of("error", ex.getMessage())
            );
        }
    }

    public UserResponse login(LoginRequest request) {
        log.info("Login attempt for email={}", request.getEmail());
        try {
            Optional<User> optionalUser = repository.findByEmail(request.getEmail());
            if (optionalUser.isEmpty()) {
                log.warn("Login failed: user not found email={}", request.getEmail());
                throw new ApiException(
                        HttpStatus.UNAUTHORIZED,
                        "Login failed",
                        Map.of("email", "User not found")
                );
            }

            User user = optionalUser.get();
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                log.warn("Login failed: incorrect password email={}", request.getEmail());
                throw new ApiException(
                        HttpStatus.UNAUTHORIZED,
                        "Login failed",
                        Map.of("password", "Incorrect password")
                );
            }

            log.info("Login successful userId={} email={}", user.getUserId(), user.getEmail());
            return UserResponse.from(user);
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Login failed for email={}", request.getEmail(), ex);
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Login failed",
                    Map.of("error", ex.getMessage())
            );
        }
    }

    public UserResponse getUser(Long id) {
        log.info("Fetching user details userId={}", id);
        try {
            Optional<User> optionalUser = repository.findUserById(id);
            if (optionalUser.isEmpty()) {
                log.warn("User not found userId={}", id);
                throw new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Unable to fetch user",
                        Map.of("userId", "User not found")
                );
            }

            User user = optionalUser.get();
            user.setAge(calculateAge(user.getDateOfBirth()));
            log.debug("User details loaded userId={}", id);
            return UserResponse.from(user);
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unable to fetch user userId={}", id, ex);
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to fetch user",
                    Map.of("error", ex.getMessage())
            );
        }
    }

    @Transactional
    public MessageResponse updatePassword(Long id, UpdatePasswordRequest request) {
        log.info("Password update attempt userId={}", id);
        try {
            Optional<User> optionalUser = repository.findUserById(id);
            if (optionalUser.isEmpty()) {
                log.warn("Password update failed: user not found userId={}", id);
                throw new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Password update failed",
                        Map.of("userId", "User not found")
                );
            }

            User user = optionalUser.get();
            if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
                log.warn("Password update failed: new password same as old userId={}", id);
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "Password update failed",
                        Map.of("newPassword", "New password cannot be the same as old password")
                );
            }

            String hashedPassword = passwordEncoder.encode(request.getNewPassword());
            int updated = repository.updatePasswordById(id, hashedPassword);
            if (updated == 0) {
                log.warn("Password update failed: no rows updated userId={}", id);
                throw new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Password update failed",
                        Map.of("userId", "User not found")
                );
            }

            log.info("Password updated successfully userId={}", id);
            return new MessageResponse("Password updated successfully!");
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Password update failed userId={}", id, ex);
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Password update failed",
                    Map.of("error", ex.getMessage())
            );
        }
    }

    @Transactional
    public MessageResponse updateUsername(Long id, UpdateUsernameRequest request) {
        log.info("Username update attempt userId={}", id);
        try {
            Optional<User> optionalUser = repository.findUserById(id);
            if (optionalUser.isEmpty()) {
                log.warn("Username update failed: user not found userId={}", id);
                throw new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Username update failed",
                        Map.of("userId", "User not found")
                );
            }

            User user = optionalUser.get();
            if (user.getUsername().equals(request.getNewUsername())) {
                log.warn("Username update failed: same username userId={}", id);
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "Username update failed",
                        Map.of("newUsername", "New username cannot be the same as old username")
                );
            }

            int updated = repository.updateUsernameById(id, request.getNewUsername());
            if (updated == 0) {
                log.warn("Username update failed: no rows updated userId={}", id);
                throw new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Username update failed",
                        Map.of("userId", "User not found")
                );
            }

            log.info("Username updated successfully userId={} newUsername={}", id, request.getNewUsername());
            return new MessageResponse("Username updated successfully!");
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Username update failed userId={}", id, ex);
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Username update failed",
                    Map.of("error", ex.getMessage())
            );
        }
    }

    private int calculateAge(LocalDate dateOfBirth) {
        try {
            if (dateOfBirth == null) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "Registration failed",
                        Map.of("dateOfBirth", "Date of birth is required")
                );
            }
            int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
            if (age < 0) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "Registration failed",
                        Map.of("dateOfBirth", "Invalid date of birth")
                );
            }
            return age;
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Age calculation failed dateOfBirth={}", dateOfBirth, ex);
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Registration failed",
                    Map.of("dateOfBirth", "Unable to calculate age: " + ex.getMessage())
            );
        }
    }
}
