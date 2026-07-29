package com.example.AuthProject.service;

import com.example.AuthProject.cache.UserCacheService;
import com.example.AuthProject.debug.AgentDebugLog;
import com.example.AuthProject.dto.ApiResponse;
import com.example.AuthProject.dto.AuthResponse;
import com.example.AuthProject.dto.DeleteUserRequest;
import com.example.AuthProject.dto.LoginRequest;
import com.example.AuthProject.dto.RegisterRequest;
import com.example.AuthProject.dto.ResendOtpRequest;
import com.example.AuthProject.dto.UpdatePasswordRequest;
import com.example.AuthProject.dto.UpdateUsernameRequest;
import com.example.AuthProject.dto.UserResponse;
import com.example.AuthProject.dto.VerifyOtpRequest;
import com.example.AuthProject.entity.User;
import com.example.AuthProject.exception.ApiException;
import com.example.AuthProject.repository.UserRepository;
import com.example.AuthProject.security.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class AuthService {
    private static final String GENERIC_LOGIN_ERROR = "Invalid email or password";

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final UserCacheService userCacheService;
    private final JwtService jwtService;
    private final OtpService otpService;

    public AuthService(
            UserRepository repository,
            PasswordEncoder passwordEncoder,
            UserCacheService userCacheService,
            JwtService jwtService,
            OtpService otpService
    ) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.userCacheService = userCacheService;
        this.jwtService = jwtService;
        this.otpService = otpService;
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
        user.setVerified(false);

        repository.save(user);
        // #region agent log
        AgentDebugLog.log("D", "AuthService.register:saved", "user_registered_unverified", Map.of(
                "userId", String.valueOf(user.getUserId()),
                "verified", String.valueOf(user.isVerified())
        ));
        // #endregion
        otpService.issueOtp(request.getEmail());

        log.info("User registered successfully email={}", request.getEmail());
        return ApiResponse.success(
                HttpStatus.CREATED,
                "Registered successfully. Please verify the OTP sent to your email."
        );
    }

    public ApiResponse<Void> verifyOtp(VerifyOtpRequest request) {
        return otpService.verifyOtp(request.getEmail(), request.getOtp());
    }

    public ApiResponse<Void> resendOtp(ResendOtpRequest request) {
        return otpService.resendOtp(request.getEmail());
    }

    public ApiResponse<AuthResponse> login(LoginRequest request) {
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
        // #region agent log
        AgentDebugLog.log("C", "AuthService.login:loaded", "user_loaded_for_login", Map.of(
                "userId", String.valueOf(user.getUserId()),
                "verified", String.valueOf(user.isVerified())
        ));
        // #endregion
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed: incorrect password email={}", request.getEmail());
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "Login failed",
                    Map.of("credentials", GENERIC_LOGIN_ERROR)
            );
        }

        if (!user.isVerified()) {
            // #region agent log
            AgentDebugLog.log("A", "AuthService.login:blocked", "login_blocked_unverified", Map.of(
                    "userId", String.valueOf(user.getUserId()),
                    "verified", "false"
            ));
            // #endregion
            log.warn("Login failed: email not verified email={}", request.getEmail());
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Login failed",
                    Map.of(
                            "verification",
                            "Please verify your email with the OTP before logging in"
                    )
            );
        }

        // #region agent log
        AgentDebugLog.log("C", "AuthService.login:success", "login_allowed_verified", Map.of(
                "userId", String.valueOf(user.getUserId()),
                "verified", "true"
        ));
        // #endregion

        user.setAge();
        UserResponse userResponse = UserResponse.from(user);
        userCacheService.putUserById(userResponse);

        String token = jwtService.generateToken(user.getEmail());
        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(userResponse)
                .build();

        log.info("Login successful userId={} email={}", user.getUserId(), user.getEmail());
        return ApiResponse.success(HttpStatus.OK, "Login successful", authResponse);
    }

    public ApiResponse<UserResponse> getUser(String email) {
        assertSelf(email);
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
        userCacheService.putUserById(userResponse);
        return ApiResponse.success(HttpStatus.OK, "User fetched successfully", userResponse);
    }

    @Transactional
    public ApiResponse<Void> updatePassword(String email, UpdatePasswordRequest request) {
        assertSelf(email);
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

        userCacheService.evictUserById(user.getUserId());

        log.info("Password updated successfully email={}", email);
        return ApiResponse.success(HttpStatus.OK, "Password updated successfully!");
    }

    @Transactional
    public ApiResponse<Void> updateUsername(String email, UpdateUsernameRequest request) {
        assertSelf(email);
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
        userCacheService.evictUserById(user.getUserId());
        userCacheService.putUserById(UserResponse.from(user));

        log.info("Username updated successfully email={} newUsername={}", email, request.getNewUsername());
        return ApiResponse.success(HttpStatus.OK, "Username updated successfully!");
    }

    @Transactional
    public ApiResponse<Void> deleteUser(String email, DeleteUserRequest request) {
        assertSelf(email);
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

        Long userId = user.getUserId();
        int deleted = repository.deleteUserByEmail(email);
        if (deleted == 0) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    "User deletion failed",
                    Map.of("email", "User not found")
            );
        }

        userCacheService.evictUserById(userId);

        log.info("User deleted successfully email={}", email);
        return ApiResponse.success(HttpStatus.OK, "User deleted successfully!");
    }

    private void assertSelf(String email) {
        String authenticatedEmail = currentEmail();
        if (authenticatedEmail == null || !authenticatedEmail.equalsIgnoreCase(email)) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Access denied",
                    Map.of("auth", "You can only access your own account")
            );
        }
    }

    private String currentEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            return userDetails.getUsername();
        }
        if (principal instanceof String name && !"anonymousUser".equals(name)) {
            return name;
        }
        return null;
    }
}
