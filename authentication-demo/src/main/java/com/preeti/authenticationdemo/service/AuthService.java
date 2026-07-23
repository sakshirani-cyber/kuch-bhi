package com.preeti.authenticationdemo.service;

import com.preeti.authenticationdemo.dto.DeleteUserRequest;
import com.preeti.authenticationdemo.dto.LoginRequest;
import com.preeti.authenticationdemo.dto.SignupRequest;
import com.preeti.authenticationdemo.dto.UpdateEmailRequest;
import com.preeti.authenticationdemo.dto.UpdatePasswordRequest;
import com.preeti.authenticationdemo.dto.UpdatePhoneNumberRequest;
import com.preeti.authenticationdemo.dto.UpdateUsernameRequest;
import com.preeti.authenticationdemo.exception.InvalidCredentialsException;
import com.preeti.authenticationdemo.exception.UserAlreadyExistsException;
import com.preeti.authenticationdemo.model.User;
import com.preeti.authenticationdemo.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MongoTemplate mongoTemplate;

    public AuthService(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        MongoTemplate mongoTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mongoTemplate = mongoTemplate;
    }

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
                age
        );

        mongoTemplate.insert(newUser);

        log.info("User '{}' registered successfully", newUser.getUsername());

        return "User registered successfully. Age on record: " + newUser.getAge();
    }

    public String login(LoginRequest request) {
        String username = request.getUsername().trim();
        log.info("Login attempt for username '{}'", username);

        Optional<User> userOptional = findUserByUsername(username);

        if (userOptional.isEmpty() || !passwordEncoder.matches(request.getPassword(), userOptional.get().getPassword())) {
            log.warn("Login failed for username '{}': invalid credentials", username);
            throw new InvalidCredentialsException("Invalid username or password");
        }

        log.info("Login successful for username '{}'", username);
        return "Login successful";
    }

    @CacheEvict(value = "users", key = "#request.currentUsername")
    public String updateUsername(UpdateUsernameRequest request) {
        User currentUser = verifyIdentityOrThrow(request.getCurrentUsername(), request.getCurrentPassword());
        String newUsername = request.getNewUsername().trim();

        if (!newUsername.equals(currentUser.getUsername())) {
            ensureUsernameIsAvailable(newUsername);
            applyFieldUpdate(currentUser.getUsername(), "username", newUsername);
        }

        log.info("Username updated for '{}' -> '{}'", request.getCurrentUsername(), newUsername);
        return "Username updated successfully";
    }

    @CacheEvict(value = "users", key = "#request.currentUsername")
    public String updatePassword(UpdatePasswordRequest request) {
        User currentUser = verifyIdentityOrThrow(request.getCurrentUsername(), request.getCurrentPassword());
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());

        applyFieldUpdate(currentUser.getUsername(), "password", encodedPassword);

        log.info("Password updated for '{}'", request.getCurrentUsername());
        return "Password updated successfully";
    }

    @CacheEvict(value = "users", key = "#request.currentUsername")
    public String updateEmail(UpdateEmailRequest request) {
        User currentUser = verifyIdentityOrThrow(request.getCurrentUsername(), request.getCurrentPassword());
        String newEmail = normalizeEmail(request.getNewEmail());

        if (!newEmail.equals(currentUser.getEmail())) {
            ensureEmailIsAvailable(newEmail);
            applyFieldUpdate(currentUser.getUsername(), "email", newEmail);
        }

        log.info("Email updated for '{}'", request.getCurrentUsername());
        return "Email updated successfully";
    }

    @CacheEvict(value = "users", key = "#request.currentUsername")
    public String updatePhoneNumber(UpdatePhoneNumberRequest request) {
        User currentUser = verifyIdentityOrThrow(request.getCurrentUsername(), request.getCurrentPassword());
        String newPhoneNumber = request.getNewPhoneNumber().trim();

        if (!newPhoneNumber.equals(currentUser.getPhoneNumber())) {
            ensurePhoneNumberIsAvailable(newPhoneNumber);
            applyFieldUpdate(currentUser.getUsername(), "phoneNumber", newPhoneNumber);
        }

        log.info("Phone number updated for '{}'", request.getCurrentUsername());
        return "Phone number updated successfully";
    }

    @CacheEvict(value = "users", key = "#request.username")
    public String deleteUser(DeleteUserRequest request) {
        User currentUser = verifyIdentityOrThrow(request.getUsername(), request.getPassword());

        Query query = Query.query(Criteria.where("username").is(currentUser.getUsername()));
        mongoTemplate.remove(query, User.class);

        log.warn("Account deleted for username '{}'", request.getUsername());
        return "Account deleted successfully";
    }

    @Cacheable(value = "users", key = "#username")
    public Optional<User> findUserByUsername(String username) {
        log.debug("Cache MISS for username '{}', fetching from database", username);
        return userRepository.findByUsername(username);
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

}
