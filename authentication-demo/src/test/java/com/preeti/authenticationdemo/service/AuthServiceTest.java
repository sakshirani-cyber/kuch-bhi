package com.preeti.authenticationdemo.service;

import com.preeti.authenticationdemo.dto.LoginRequest;
import com.preeti.authenticationdemo.dto.SignupRequest;
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

    @InjectMocks
    private AuthService authService;

    private SignupRequest validSignupRequest;
    private User testUser;

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
                26
        );
    }

    @Test
    @DisplayName("Signup: Valid user registration succeeds")
    void signup_Success() {
        when(userRepository.findByUsername("Meena")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("meena234@cdjnd.com")).thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumber("4564678431")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password1!")).thenReturn("encodedPassword1!");

        String result = authService.signup(validSignupRequest);

        assertNotNull(result);
        verify(mongoTemplate).insert(any(User.class));
    }

    @Test
    @DisplayName("Signup: Underage user (< 13 years old) is rejected")
    void signup_UnderageUser_ThrowsException() {
        SignupRequest underageRequest = new SignupRequest(
                "YoungUser",
                "Password1!",
                "young@example.com",
                "1234567890",
                LocalDate.now().minusYears(10) // 10 years old
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
    @DisplayName("Login: Missing username returns generic 'Invalid username or password' (prevents user enumeration)")
    void login_UserNotFound_ThrowsInvalidCredentialsException() {
        LoginRequest loginRequest = new LoginRequest("NonExistentUser", "Password1!");
        when(userRepository.findByUsername("NonExistentUser")).thenReturn(Optional.empty());

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(loginRequest)
        );

        assertEquals("Invalid username or password", exception.getMessage());
    }

    @Test
    @DisplayName("Login: Incorrect password returns exact same 'Invalid username or password'")
    void login_WrongPassword_ThrowsInvalidCredentialsException() {
        LoginRequest loginRequest = new LoginRequest("Meena", "WrongPassword!");
        when(userRepository.findByUsername("Meena")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("WrongPassword!", "encodedPassword1!")).thenReturn(false);

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(loginRequest)
        );

        assertEquals("Invalid username or password", exception.getMessage());
    }

    @Test
    @DisplayName("Login: Valid credentials login successfully")
    void login_Success() {
        LoginRequest loginRequest = new LoginRequest("Meena", "Password1!");
        when(userRepository.findByUsername("Meena")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Password1!", "encodedPassword1!")).thenReturn(true);

        String result = authService.login(loginRequest);

        assertEquals("Login successful", result);
    }

}
