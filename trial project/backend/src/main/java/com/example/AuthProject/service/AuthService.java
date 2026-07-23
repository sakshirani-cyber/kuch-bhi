package com.example.AuthProject.service;

import com.example.AuthProject.cache.UserCacheService;
import com.example.AuthProject.dto.ApiResponse;
import com.example.AuthProject.dto.DeleteUserRequest;
import com.example.AuthProject.dto.LoginRequest;
import com.example.AuthProject.dto.RegisterRequest;
import com.example.AuthProject.dto.UpdatePasswordRequest;
import com.example.AuthProject.dto.UpdateUsernameRequest;
import com.example.AuthProject.dto.UserResponse;
import com.example.AuthProject.entity.User;
import com.example.AuthProject.exception.ApiException;
import com.example.AuthProject.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final String GENERIC_LOGIN_ERROR = "Invalid email or password";

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final UserCacheService userCacheService;

    public AuthService(
            UserRepository repository,
            PasswordEncoder passwordEncoder,
            UserCacheService userCacheService
    ) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.userCacheService = userCacheService;
    }

    @Transactional
    public ApiResponse<Void> register(RegisterRequest request) {
        log.info("Registration attempt for email={}", request.getEmail());

        if (repository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: email already exists email={}", request.getEmail());
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Registration failed",
                    Map.of("email", "User already exists")
            );
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(hashedPassword);
        user.setContactNumber(request.getContactNumber());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setAge();

        repository.save(user);

        log.info("User registered successfully email={}", request.getEmail());
        return ApiResponse.success(HttpStatus.CREATED, "User registered successfully!");
    }

    public ApiResponse<UserResponse> login(LoginRequest request) {
        log.info("Login attempt for email={}", request.getEmail());

        Optional<User> optionalUser = repository.findByEmail(request.getEmail());
        if (optionalUser.isEmpty()) {
            log.warn("Login failed: user not found email={}", request.getEmail());
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "Login failed",
                    Map.of("credentials", GENERIC_LOGIN_ERROR)
            );
        }

        User user = optionalUser.get();
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed: incorrect password email={}", request.getEmail());
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "Login failed",
                    Map.of("credentials", GENERIC_LOGIN_ERROR)
            );
        }

        user.setAge();
        UserResponse userResponse = UserResponse.from(user);
        userCacheService.put(userResponse);

        log.info("Login successful userId={} email={}", user.getUserId(), user.getEmail());
        return ApiResponse.success(HttpStatus.OK, "Login successful", userResponse);
    }

    public ApiResponse<UserResponse> getUser(String email) {
        log.info("Fetching user details email={}", email);

        Optional<UserResponse> cached = userCacheService.getByEmail(email);
        if (cached.isPresent()) {
            return ApiResponse.success(HttpStatus.OK, "User fetched successfully", cached.get());
        }

        User user = repository.findUserDetailsByEmail(email)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Unable to fetch user",
                        Map.of("email", "User not found")
                ));

        user.setAge();
        UserResponse userResponse = UserResponse.from(user);
        userCacheService.put(userResponse);
        return ApiResponse.success(HttpStatus.OK, "User fetched successfully", userResponse);
    }

    @Transactional
    public ApiResponse<Void> updatePassword(String email, UpdatePasswordRequest request) {
        log.info("Password update attempt email={}", email);

        User user = repository.findUserDetailsByEmail(email)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Password update failed",
                        Map.of("email", "User not found")
                ));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            log.warn("Password update failed: incorrect current password email={}", email);
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Password update failed",
                    Map.of("currentPassword", "Current password is incorrect")
            );
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            log.warn("Password update failed: new password same as old email={}", email);
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Password update failed",
                    Map.of("newPassword", "New password cannot be the same as old password")
            );
        }

        String hashedPassword = passwordEncoder.encode(request.getNewPassword());
        int updated = repository.updatePassword(email, hashedPassword);
        if (updated == 0) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    "Password update failed",
                    Map.of("email", "User not found")
            );
        }

        userCacheService.put(UserResponse.from(user));

        log.info("Password updated successfully email={}", email);
        return ApiResponse.success(HttpStatus.OK, "Password updated successfully!");
    }

    @Transactional
    public ApiResponse<Void> updateUsername(String email, UpdateUsernameRequest request) {
        log.info("Username update attempt email={}", email);

        User user = repository.findUserDetailsByEmail(email)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Username update failed",
                        Map.of("email", "User not found")
                ));

        if (!user.getUsername().equals(request.getCurrentUsername())) {
            log.warn("Username update failed: incorrect current username email={}", email);
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Username update failed",
                    Map.of("currentUsername", "Current username is incorrect")
            );
        }

        if (user.getUsername().equals(request.getNewUsername())) {
            log.warn("Username update failed: same username email={}", email);
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Username update failed",
                    Map.of("newUsername", "New username cannot be the same as old username")
            );
        }

        int updated = repository.updateUsernameByEmail(email, request.getNewUsername());
        if (updated == 0) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    "Username update failed",
                    Map.of("email", "User not found")
            );
        }

        user.setUsername(request.getNewUsername());
        user.setAge();
        userCacheService.put(UserResponse.from(user));

        log.info("Username updated successfully email={} newUsername={}", email, request.getNewUsername());
        return ApiResponse.success(HttpStatus.OK, "Username updated successfully!");
    }

    @Transactional
    public ApiResponse<Void> deleteUser(String email, DeleteUserRequest request) {
        log.info("Delete user attempt email={}", email);

        User user = repository.findUserDetailsByEmail(email)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "User deletion failed",
                        Map.of("email", "User not found")
                ));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("User deletion failed: incorrect password email={}", email);
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "User deletion failed",
                    Map.of("password", "Password is incorrect")
            );
        }

        int deleted = repository.deleteUserByEmail(email);
        if (deleted == 0) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    "User deletion failed",
                    Map.of("email", "User not found")
            );
        }

        userCacheService.evict(email);

        log.info("User deleted successfully email={}", email);
        return ApiResponse.success(HttpStatus.OK, "User deleted successfully!");
    }
}
