package com.preeti.authenticationdemo.service;

import com.preeti.authenticationdemo.cache.CacheService;
import com.preeti.authenticationdemo.dto.DeleteUserRequest;
import com.preeti.authenticationdemo.dto.LoginRequest;
import com.preeti.authenticationdemo.dto.SignupRequest;
import com.preeti.authenticationdemo.dto.UpdateEmailRequest;
import com.preeti.authenticationdemo.dto.UpdatePasswordRequest;
import com.preeti.authenticationdemo.dto.UpdatePhoneNumberRequest;
import com.preeti.authenticationdemo.dto.UpdateUsernameRequest;
import com.preeti.authenticationdemo.exception.InvalidCredentialsException;
import com.preeti.authenticationdemo.exception.UserAlreadyExistsException;
import com.preeti.authenticationdemo.exception.UserNotFoundException;
import com.preeti.authenticationdemo.model.User;
import com.preeti.authenticationdemo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MongoTemplate mongoTemplate;
    private final CacheService cacheService;

    public AuthService(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        MongoTemplate mongoTemplate,
                        CacheService cacheService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mongoTemplate = mongoTemplate;
        this.cacheService = cacheService;
    }

    public String signup(SignupRequest request) {

        String username = request.getUsername().trim();
        String email = request.getEmail().trim();
        String phoneNumber = request.getPhoneNumber().trim();

        logger.info("Signup attempt for username '{}'", username);

        ensureUsernameIsAvailable(username);
        ensureEmailIsAvailable(email);
        ensurePhoneNumberIsAvailable(phoneNumber);

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        int age = calculateAge(request.getDateOfBirth());

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

        // Write-through: populate the cache immediately so the very
        // first login right after signup is already a cache hit.
        cacheService.putUser(newUser);

        logger.info("User '{}' registered successfully", newUser.getUsername());

        return "User registered successfully. Age on record: " + newUser.getAge();
    }

    public String login(LoginRequest request) {

        logger.info("Login attempt for username '{}'", request.getUsername());

        User user = findUserByUsernameOrThrow(request.getUsername());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            logger.warn("Login failed for username '{}': incorrect password", request.getUsername());
            throw new InvalidCredentialsException("Invalid username or password");
        }

        logger.info("Login successful for username '{}'", request.getUsername());

        return "Login successful";
    }

    public String updateUsername(UpdateUsernameRequest request) {

        User currentUser = verifyIdentityOrThrow(request.getCurrentUsername(), request.getCurrentPassword());

        String newUsername = request.getNewUsername().trim();

        if (!newUsername.equals(currentUser.getUsername())) {
            ensureUsernameIsAvailable(newUsername);
            applyFieldUpdate(currentUser.getUsername(), "username", newUsername);

            // The cache key IS the username, so a username change means
            // evicting the old key and caching a fresh copy under the new one.
            cacheService.evictUser(currentUser.getUsername());
            currentUser.setUsername(newUsername);
            cacheService.putUser(currentUser);
        }

        logger.info("Username updated for '{}' -> '{}'", request.getCurrentUsername(), newUsername);

        return "Username updated successfully";
    }

    public String updatePassword(UpdatePasswordRequest request) {

        User currentUser = verifyIdentityOrThrow(request.getCurrentUsername(), request.getCurrentPassword());

        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        applyFieldUpdate(currentUser.getUsername(), "password", encodedPassword);

        // Same username, so just refresh the cached copy in place —
        // otherwise a cache hit would keep matching the OLD password hash.
        currentUser.setPassword(encodedPassword);
        cacheService.putUser(currentUser);

        logger.info("Password updated for '{}'", request.getCurrentUsername());

        return "Password updated successfully";
    }

    public String updateEmail(UpdateEmailRequest request) {

        User currentUser = verifyIdentityOrThrow(request.getCurrentUsername(), request.getCurrentPassword());

        String newEmail = request.getNewEmail().trim();

        if (!newEmail.equals(currentUser.getEmail())) {
            ensureEmailIsAvailable(newEmail);
            applyFieldUpdate(currentUser.getUsername(), "email", newEmail);

            currentUser.setEmail(newEmail);
            cacheService.putUser(currentUser);
        }

        logger.info("Email updated for '{}'", request.getCurrentUsername());

        return "Email updated successfully";
    }

    public String updatePhoneNumber(UpdatePhoneNumberRequest request) {

        User currentUser = verifyIdentityOrThrow(request.getCurrentUsername(), request.getCurrentPassword());

        String newPhoneNumber = request.getNewPhoneNumber().trim();

        if (!newPhoneNumber.equals(currentUser.getPhoneNumber())) {
            ensurePhoneNumberIsAvailable(newPhoneNumber);
            applyFieldUpdate(currentUser.getUsername(), "phoneNumber", newPhoneNumber);

            currentUser.setPhoneNumber(newPhoneNumber);
            cacheService.putUser(currentUser);
        }

        logger.info("Phone number updated for '{}'", request.getCurrentUsername());

        return "Phone number updated successfully";
    }

    public String deleteUser(DeleteUserRequest request) {

        User currentUser = verifyIdentityOrThrow(request.getUsername(), request.getPassword());

        Query query = Query.query(Criteria.where("username").is(currentUser.getUsername()));
        mongoTemplate.remove(query, User.class);

        cacheService.evictUser(currentUser.getUsername());

        logger.warn("Account deleted for username '{}'", request.getUsername());

        return "Account deleted successfully";
    }

    private User verifyIdentityOrThrow(String username, String password) {

        User user = findUserByUsernameOrThrow(username);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            logger.warn("Identity check failed for username '{}': incorrect password", username);
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        return user;
    }

    /**
     * Cache-aside read: check Caffeine in-memory cache first, and only fall through to
     * MongoDB on a cache miss.
     */
    private User findUserByUsernameOrThrow(String username) {

        Optional<User> cachedUser = cacheService.getUser(username);
        if (cachedUser.isPresent()) {
            return cachedUser.get();
        }

        Optional<User> userFromDatabase = userRepository.findByUsernameManual(username);

        User user = userFromDatabase.orElseThrow(() -> {
            logger.warn("No account found for username '{}'", username);
            return new UserNotFoundException("No account found with that username");
        });

        cacheService.putUser(user);

        return user;
    }

    private void ensureUsernameIsAvailable(String username) {
        userRepository.findByUsernameManual(username).ifPresent(existingUser -> {
            logger.warn("Rejected: username '{}' is already taken", username);
            throw new UserAlreadyExistsException("Username '" + username + "' is already taken");
        });
    }

    private void ensureEmailIsAvailable(String email) {
        userRepository.findByEmailManual(email).ifPresent(existingUser -> {
            logger.warn("Rejected: email is already in use");
            throw new UserAlreadyExistsException("An account with this email already exists");
        });
    }

    private void ensurePhoneNumberIsAvailable(String phoneNumber) {
        userRepository.findByPhoneNumberManual(phoneNumber).ifPresent(existingUser -> {
            logger.warn("Rejected: phone number is already in use");
            throw new UserAlreadyExistsException("An account with this phone number already exists");
        });
    }

    /**
     * Every single-field update always has exactly one non-blank
     * new value (guaranteed by @NotBlank on the DTO), so the update
     * document sent to Mongo can never be empty here.
     */
    private void applyFieldUpdate(String username, String fieldName, String newValue) {
        Query query = Query.query(Criteria.where("username").is(username));
        Update update = new Update().set(fieldName, newValue);
        mongoTemplate.updateFirst(query, update, User.class);
    }

    private int calculateAge(LocalDate dateOfBirth) {
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

}
