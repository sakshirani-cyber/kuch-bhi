package com.preeti.authenticationdemo.service;

import com.preeti.authenticationdemo.cache.CacheService;
import com.preeti.authenticationdemo.dto.DeleteUserRequest;
import com.preeti.authenticationdemo.dto.LoginRequest;
import com.preeti.authenticationdemo.dto.ResendOtpRequest;
import com.preeti.authenticationdemo.dto.SignupRequest;
import com.preeti.authenticationdemo.dto.UpdateEmailRequest;
import com.preeti.authenticationdemo.dto.UpdatePasswordRequest;
import com.preeti.authenticationdemo.dto.UpdatePhoneNumberRequest;
import com.preeti.authenticationdemo.dto.UpdateUsernameRequest;
import com.preeti.authenticationdemo.dto.VerifyOtpRequest;
import com.preeti.authenticationdemo.exception.InvalidCredentialsException;
import com.preeti.authenticationdemo.exception.UserAlreadyExistsException;
import com.preeti.authenticationdemo.model.User;
import com.preeti.authenticationdemo.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MongoTemplate mongoTemplate;
    private final CacheService cacheService;
    private final OtpNotificationService otpNotificationService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       MongoTemplate mongoTemplate,
                       CacheService cacheService,
                       OtpNotificationService otpNotificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mongoTemplate = mongoTemplate;
        this.cacheService = cacheService;
        this.otpNotificationService = otpNotificationService;
    }

    @Transactional
    public String signup(SignupRequest request) {
        String username = request.getUsername().trim();
        String email = normalizeEmail(request.getEmail());
        String phoneNumber = request.getPhoneNumber().trim();

        log.info("Signup attempt for username '{}'", username);

        ensureUsernameIsAvailable(username);
        ensureEmailIsAvailable(email);
        ensurePhoneNumberIsAvailable(phoneNumber);

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        int age = calculateAge(request.getDateOfBirth());
        if (age < 13) {
            log.warn("Rejected: user age {} is under minimum required age of 13", age);
            throw new IllegalArgumentException("You must be at least 13 years old to register");
        }

        User newUser = new User(
                null,
                username,
                encodedPassword,
                email,
                phoneNumber,
                request.getDateOfBirth(),
                age,
                false
        );

        mongoTemplate.insert(newUser);
        log.info("User '{}' registered successfully. Generating OTP...", newUser.getUsername());

        String otp = generateOtpCode();
        cacheService.saveOtp(email, otp);
        otpNotificationService.sendOtp(email, otp);

        return "User registered successfully. Please verify the OTP sent to your email.";
    }

    public String login(LoginRequest request) {
        String username = request.getUsername().trim();
        log.info("Login attempt for username '{}'", username);

        Optional<User> userOptional = findUserByUsername(username);

        if (userOptional.isEmpty() || !passwordEncoder.matches(request.getPassword(), userOptional.get().getPassword())) {
            log.warn("Login failed for username '{}': invalid credentials", username);
            throw new InvalidCredentialsException("Invalid username or password");
        }

        User user = userOptional.get();
        if (!user.isVerified()) {
            log.warn("Login blocked for username '{}': account is unverified", username);
            throw new IllegalStateException("Account not verified. Please verify your OTP first.");
        }

        log.info("Login successful for username '{}'", username);
        return "Login successful";
    }

    @Transactional
    public String verifyOtp(VerifyOtpRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        log.info("OTP verification request for email '{}'", normalizedEmail);

        Optional<String> cachedOtpOptional = cacheService.getOtp(normalizedEmail);
        if (cachedOtpOptional.isEmpty()) {
            log.warn("OTP verification failed for email '{}': OTP expired or missing", normalizedEmail);
            throw new IllegalArgumentException("OTP has expired or does not exist. Please request a new OTP.");
        }

        int currentAttempts = cacheService.getAttempts(normalizedEmail);
        if (currentAttempts >= 3) {
            cacheService.invalidateOtp(normalizedEmail);
            log.warn("OTP verification blocked for email '{}': maximum attempts (3) reached", normalizedEmail);
            throw new IllegalArgumentException("Maximum OTP verification attempts (3) exceeded. Please request a new OTP.");
        }

        String cachedOtp = cachedOtpOptional.get();
        if (!cachedOtp.equals(request.getOtp().trim())) {
            int newAttempts = cacheService.incrementAttempts(normalizedEmail);
            log.warn("Wrong OTP entered for email '{}'. Failed attempt count: {}", normalizedEmail, newAttempts);
            if (newAttempts >= 3) {
                cacheService.invalidateOtp(normalizedEmail);
                throw new IllegalArgumentException("Maximum OTP verification attempts (3) exceeded. Please request a new OTP.");
            }
            throw new IllegalArgumentException("Invalid OTP code. Please try again.");
        }

        Optional<User> userOptional = userRepository.findByEmail(normalizedEmail);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User with email '" + normalizedEmail + "' not found.");
        }

        User user = userOptional.get();
        user.setVerified(true);
        userRepository.save(user);

        cacheService.invalidateOtp(normalizedEmail);
        cacheService.evictUser(user.getUsername());

        log.info("User with email '{}' verified successfully", normalizedEmail);
        return "OTP verified successfully. Your account is now verified and active.";
    }

    public String resendOtp(ResendOtpRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        log.info("Resend OTP request for email '{}'", normalizedEmail);

        Optional<User> userOptional = userRepository.findByEmail(normalizedEmail);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User with email '" + normalizedEmail + "' not found.");
        }

        User user = userOptional.get();
        if (user.isVerified()) {
            return "Account is already verified.";
        }

        cacheService.invalidateOtp(normalizedEmail);
        String newOtp = generateOtpCode();
        cacheService.saveOtp(normalizedEmail, newOtp);
        otpNotificationService.sendOtp(normalizedEmail, newOtp);

        log.info("Resent new OTP to email '{}'", normalizedEmail);
        return "New OTP sent successfully.";
    }

    @Transactional
    public String updateUsername(UpdateUsernameRequest request) {
        User currentUser = verifyIdentityOrThrow(request.getCurrentUsername(), request.getCurrentPassword());
        String newUsername = request.getNewUsername().trim();

        if (!newUsername.equals(currentUser.getUsername())) {
            ensureUsernameIsAvailable(newUsername);
            applyFieldUpdate(currentUser.getUsername(), "username", newUsername);
        }

        cacheService.evictUser(request.getCurrentUsername());
        cacheService.evictUser(newUsername);

        log.info("Username updated for '{}' -> '{}'", request.getCurrentUsername(), newUsername);
        return "Username updated successfully";
    }

    @Transactional
    public String updatePassword(UpdatePasswordRequest request) {
        User currentUser = verifyIdentityOrThrow(request.getCurrentUsername(), request.getCurrentPassword());
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());

        applyFieldUpdate(currentUser.getUsername(), "password", encodedPassword);
        cacheService.evictUser(request.getCurrentUsername());

        log.info("Password updated for '{}'", request.getCurrentUsername());
        return "Password updated successfully";
    }

    @Transactional
    public String updateEmail(UpdateEmailRequest request) {
        User currentUser = verifyIdentityOrThrow(request.getCurrentUsername(), request.getCurrentPassword());
        String newEmail = normalizeEmail(request.getNewEmail());

        if (!newEmail.equals(currentUser.getEmail())) {
            ensureEmailIsAvailable(newEmail);
            applyFieldUpdate(currentUser.getUsername(), "email", newEmail);
        }

        cacheService.evictUser(request.getCurrentUsername());

        log.info("Email updated for '{}'", request.getCurrentUsername());
        return "Email updated successfully";
    }

    @Transactional
    public String updatePhoneNumber(UpdatePhoneNumberRequest request) {
        User currentUser = verifyIdentityOrThrow(request.getCurrentUsername(), request.getCurrentPassword());
        String newPhoneNumber = request.getNewPhoneNumber().trim();

        if (!newPhoneNumber.equals(currentUser.getPhoneNumber())) {
            ensurePhoneNumberIsAvailable(newPhoneNumber);
            applyFieldUpdate(currentUser.getUsername(), "phoneNumber", newPhoneNumber);
        }

        cacheService.evictUser(request.getCurrentUsername());

        log.info("Phone number updated for '{}'", request.getCurrentUsername());
        return "Phone number updated successfully";
    }

    @Transactional
    public String deleteUser(DeleteUserRequest request) {
        User currentUser = verifyIdentityOrThrow(request.getUsername(), request.getPassword());

        Query query = Query.query(Criteria.where("username").is(currentUser.getUsername()));
        mongoTemplate.remove(query, User.class);

        cacheService.evictUser(request.getUsername());

        log.warn("Account deleted for username '{}'", request.getUsername());
        return "Account deleted successfully";
    }

    public Optional<User> findUserByUsername(String username) {
        Optional<User> cachedUser = cacheService.getUser(username);
        if (cachedUser.isPresent()) {
            return cachedUser;
        }

        log.debug("Cache MISS for username '{}', fetching from database", username);
        Optional<User> userOptional = userRepository.findByUsername(username);
        userOptional.ifPresent(cacheService::putUser);
        return userOptional;
    }

    private User verifyIdentityOrThrow(String username, String password) {
        Optional<User> userOptional = findUserByUsername(username);

        if (userOptional.isEmpty() || !passwordEncoder.matches(password, userOptional.get().getPassword())) {
            log.warn("Identity check failed for username '{}': incorrect password", username);
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        return userOptional.get();
    }

    private void ensureUsernameIsAvailable(String username) {
        userRepository.findByUsername(username).ifPresent(existingUser -> {
            log.warn("Rejected: username '{}' is already taken", username);
            throw new UserAlreadyExistsException("Username '" + username + "' is already taken");
        });
    }

    private void ensureEmailIsAvailable(String email) {
        userRepository.findByEmail(email).ifPresent(existingUser -> {
            log.warn("Rejected: email is already in use");
            throw new UserAlreadyExistsException("An account with this email already exists");
        });
    }

    private void ensurePhoneNumberIsAvailable(String phoneNumber) {
        userRepository.findByPhoneNumber(phoneNumber).ifPresent(existingUser -> {
            log.warn("Rejected: phone number is already in use");
            throw new UserAlreadyExistsException("An account with this phone number already exists");
        });
    }

    private void applyFieldUpdate(String username, String fieldName, String newValue) {
        Query query = Query.query(Criteria.where("username").is(username));
        Update update = new Update().set(fieldName, newValue);
        mongoTemplate.updateFirst(query, update, User.class);
    }

    private String normalizeEmail(String email) {
        return email != null ? email.trim().toLowerCase() : "";
    }

    private int calculateAge(LocalDate dateOfBirth) {
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    private String generateOtpCode() {
        int otpInt = secureRandom.nextInt(1000000);
        return String.format("%06d", otpInt);
    }

}
