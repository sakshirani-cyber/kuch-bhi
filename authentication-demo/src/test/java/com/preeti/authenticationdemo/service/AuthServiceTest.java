package com.preeti.authenticationdemo.service;

import com.preeti.authenticationdemo.cache.CacheService;
import com.preeti.authenticationdemo.dto.LoginRequest;
import com.preeti.authenticationdemo.dto.SignupRequest;
import com.preeti.authenticationdemo.dto.VerifyOtpRequest;
import com.preeti.authenticationdemo.exception.InvalidCredentialsException;
import com.preeti.authenticationdemo.exception.UserAlreadyExistsException;
import com.preeti.authenticationdemo.model.User;
import com.preeti.authenticationdemo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private CacheService cacheService;

    @Mock
    private OtpNotificationService otpNotificationService;

    @InjectMocks
    private AuthService authService;

    private SignupRequest validSignupRequest;
    private User testUser;
    private User verifiedTestUser;

    @BeforeEach
    void setUp() {
        validSignupRequest = new SignupRequest(
                "Meena",
                "Password1!",
                "meena234@cdjnd.com",
                "4564678431",
                LocalDate.of(2000, 1, 1)
        );

        testUser = new User(
                "123",
                "Meena",
                "encodedPassword1!",
                "meena234@cdjnd.com",
                "4564678431",
                LocalDate.of(2000, 1, 1),
                26,
                false
        );

        verifiedTestUser = new User(
                "123",
                "Meena",
                "encodedPassword1!",
                "meena234@cdjnd.com",
                "4564678431",
                LocalDate.of(2000, 1, 1),
                26,
                true
        );
    }

    @Test
    @DisplayName("Signup: Valid user registration generates OTP and triggers async notification")
    void signup_Success() {
        when(userRepository.findByUsername("Meena")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("meena234@cdjnd.com")).thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumber("4564678431")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password1!")).thenReturn("encodedPassword1!");

        String result = authService.signup(validSignupRequest);

        assertNotNull(result);
        verify(mongoTemplate).insert(any(User.class));
        verify(cacheService).saveOtp(eq("meena234@cdjnd.com"), anyString());
        verify(otpNotificationService).sendOtp(eq("meena234@cdjnd.com"), anyString());
    }

    @Test
    @DisplayName("Signup: Underage user (< 13 years old) is rejected")
    void signup_UnderageUser_ThrowsException() {
        SignupRequest underageRequest = new SignupRequest(
                "YoungUser",
                "Password1!",
                "young@example.com",
                "1234567890",
                LocalDate.now().minusYears(10)
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.signup(underageRequest)
        );

        assertEquals("You must be at least 13 years old to register", exception.getMessage());
    }

    @Test
    @DisplayName("Signup: Duplicate email is rejected with UserAlreadyExistsException")
    void signup_DuplicateEmail_ThrowsException() {
        when(userRepository.findByUsername("Meena")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("meena234@cdjnd.com")).thenReturn(Optional.of(testUser));

        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> authService.signup(validSignupRequest)
        );

        assertEquals("An account with this email already exists", exception.getMessage());
    }

    @Test
    @DisplayName("Login: Unverified user login is blocked")
    void login_UnverifiedUser_ThrowsIllegalStateException() {
        LoginRequest loginRequest = new LoginRequest("Meena", "Password1!");
        when(cacheService.getUser("Meena")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("Meena")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Password1!", "encodedPassword1!")).thenReturn(true);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> authService.login(loginRequest)
        );

        assertEquals("Account not verified. Please verify your OTP first.", exception.getMessage());
    }

    @Test
    @DisplayName("Login: Verified user logs in successfully")
    void login_VerifiedUser_Success() {
        LoginRequest loginRequest = new LoginRequest("Meena", "Password1!");
        when(cacheService.getUser("Meena")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("Meena")).thenReturn(Optional.of(verifiedTestUser));
        when(passwordEncoder.matches("Password1!", "encodedPassword1!")).thenReturn(true);

        String result = authService.login(loginRequest);

        assertEquals("Login successful", result);
        verify(cacheService).putUser(verifiedTestUser);
    }

    @Test
    @DisplayName("Verify OTP: Valid OTP marks user verified and invalidates OTP cache")
    void verifyOtp_Success() {
        VerifyOtpRequest request = new VerifyOtpRequest("meena234@cdjnd.com", "123456");
        when(cacheService.getOtp("meena234@cdjnd.com")).thenReturn(Optional.of("123456"));
        when(cacheService.getAttempts("meena234@cdjnd.com")).thenReturn(0);
        when(userRepository.findByEmail("meena234@cdjnd.com")).thenReturn(Optional.of(testUser));

        String result = authService.verifyOtp(request);

        assertEquals("OTP verified successfully. Your account is now verified and active.", result);
        verify(userRepository).save(testUser);
        verify(cacheService).invalidateOtp("meena234@cdjnd.com");
    }

    @Test
    @DisplayName("Verify OTP: Wrong OTP increments attempt count and throws IllegalArgumentException")
    void verifyOtp_WrongOtp_IncrementsAttempts() {
        VerifyOtpRequest request = new VerifyOtpRequest("meena234@cdjnd.com", "999999");
        when(cacheService.getOtp("meena234@cdjnd.com")).thenReturn(Optional.of("123456"));
        when(cacheService.getAttempts("meena234@cdjnd.com")).thenReturn(0);
        when(cacheService.incrementAttempts("meena234@cdjnd.com")).thenReturn(1);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.verifyOtp(request)
        );

        assertEquals("Invalid OTP code. Please try again.", exception.getMessage());
    }

}
