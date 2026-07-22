package com.preeti.authenticationdemo.service;

import com.preeti.authenticationdemo.dto.LoginRequest;
import com.preeti.authenticationdemo.dto.SignupRequest;
import com.preeti.authenticationdemo.dto.UpdateRequest;
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

import java.util.Optional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

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

        logger.info("Signup attempt for username '{}'", request.getUsername());

        ensureUsernameIsAvailable(request.getUsername());
        ensureEmailIsAvailable(request.getEmail());
        ensurePhoneNumberIsAvailable(request.getPhoneNumber());

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User newUser = new User(
                null,
                request.getUsername(),
                encodedPassword,
                request.getEmail(),
                request.getPhoneNumber(),
                request.getDateOfBirth()
        );

        mongoTemplate.insert(newUser);

        logger.info("User '{}' registered successfully", newUser.getUsername());

        return "User registered successfully. Age on record: " + newUser.getAge();
    }

    public String login(LoginRequest request) {

        logger.info("Login attempt for username '{}'", request.getUsername());

        User user = findUserByUsernameOrThrow(request.getUsername());

        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!passwordMatches) {
            logger.warn("Login failed for username '{}': incorrect password", request.getUsername());
            throw new InvalidCredentialsException("Invalid username or password");
        }

        logger.info("Login successful for username '{}'", request.getUsername());

        return "Login successful";
    }

    public String updateCredentials(UpdateRequest request) {

        logger.info("Profile update attempt for username '{}'", request.getCurrentUsername());

        User currentUser = findUserByUsernameOrThrow(request.getCurrentUsername());

        boolean passwordMatches = passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword());

        if (!passwordMatches) {
            logger.warn("Profile update failed for username '{}': incorrect current password", request.getCurrentUsername());
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        Update update = buildUpdateFromRequest(request, currentUser);

        Query query = Query.query(Criteria.where("username").is(currentUser.getUsername()));

        mongoTemplate.updateFirst(query, update, User.class);

        logger.info("Profile updated successfully for username '{}'", request.getCurrentUsername());

        return "Profile updated successfully";
    }

    private User findUserByUsernameOrThrow(String username) {
        Optional<User> existingUser = userRepository.findByUsernameManual(username);
        return existingUser.orElseThrow(() -> {
            logger.warn("No account found for username '{}'", username);
            return new UserNotFoundException("No account found with that username");
        });
    }

    private void ensureUsernameIsAvailable(String username) {
        userRepository.findByUsernameManual(username).ifPresent(existingUser -> {
            logger.warn("Signup rejected: username '{}' is already taken", username);
            throw new UserAlreadyExistsException("Username '" + username + "' is already taken");
        });
    }

    private void ensureEmailIsAvailable(String email) {
        userRepository.findByEmailManual(email).ifPresent(existingUser -> {
            logger.warn("Signup rejected: email is already in use");
            throw new UserAlreadyExistsException("An account with this email already exists");
        });
    }

    private void ensurePhoneNumberIsAvailable(String phoneNumber) {
        userRepository.findByPhoneNumberManual(phoneNumber).ifPresent(existingUser -> {
            logger.warn("Signup rejected: phone number is already in use");
            throw new UserAlreadyExistsException("An account with this phone number already exists");
        });
    }

    private Update buildUpdateFromRequest(UpdateRequest request, User currentUser) {

        Update update = new Update();

        boolean wantsUsernameChange = isPresent(request.getNewUsername())
                && !request.getNewUsername().equals(currentUser.getUsername());

        if (wantsUsernameChange) {
            ensureUsernameIsAvailable(request.getNewUsername());
            update.set("username", request.getNewUsername());
        }

        if (isPresent(request.getNewPassword())) {
            update.set("password", passwordEncoder.encode(request.getNewPassword()));
        }

        boolean wantsEmailChange = isPresent(request.getNewEmail())
                && !request.getNewEmail().equals(currentUser.getEmail());

        if (wantsEmailChange) {
            ensureEmailIsAvailable(request.getNewEmail());
            update.set("email", request.getNewEmail());
        }

        boolean wantsPhoneChange = isPresent(request.getNewPhoneNumber())
                && !request.getNewPhoneNumber().equals(currentUser.getPhoneNumber());

        if (wantsPhoneChange) {
            ensurePhoneNumberIsAvailable(request.getNewPhoneNumber());
            update.set("phoneNumber", request.getNewPhoneNumber());
        }

        return update;
    }

    private boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }

}
