package com.preeti.authenticationdemo.service;

import com.preeti.authenticationdemo.dto.LoginRequest;
import com.preeti.authenticationdemo.dto.SignupRequest;
import com.preeti.authenticationdemo.dto.UpdateRequest;
import com.preeti.authenticationdemo.exception.InvalidCredentialsException;
import com.preeti.authenticationdemo.exception.UserAlreadyExistsException;
import com.preeti.authenticationdemo.exception.UserNotFoundException;
import com.preeti.authenticationdemo.model.User;
import com.preeti.authenticationdemo.repository.UserRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

        // Instead of repository.save(), used .insert()
        // so it's clear this always creates a brand-new document.
        mongoTemplate.insert(newUser);

        return "User registered successfully. Age on record: " + newUser.getAge();
    }

    public String login(LoginRequest request) {

        User user = findUserByUsernameOrThrow(request.getUsername());

        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!passwordMatches) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        return "Login successful";
    }

    public String updateCredentials(UpdateRequest request) {

        User currentUser = findUserByUsernameOrThrow(request.getCurrentUsername());

        boolean passwordMatches = passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword());

        if (!passwordMatches) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        Update update = buildUpdateFromRequest(request, currentUser);

        Query query = Query.query(Criteria.where("username").is(currentUser.getUsername()));

        // Explicit partial update via MongoTemplate instead of
        // fetch -> mutate object -> repository.save().
        mongoTemplate.updateFirst(query, update, User.class);

        return "Profile updated successfully";
    }

    private User findUserByUsernameOrThrow(String username) {
        Optional<User> existingUser = userRepository.findByUsernameManual(username);
        return existingUser.orElseThrow(() -> new UserNotFoundException("No account found with that username"));
    }

    private void ensureUsernameIsAvailable(String username) {
        userRepository.findByUsernameManual(username).ifPresent(existingUser -> {
            throw new UserAlreadyExistsException("Username '" + username + "' is already taken");
        });
    }

    private void ensureEmailIsAvailable(String email) {
        userRepository.findByEmailManual(email).ifPresent(existingUser -> {
            throw new UserAlreadyExistsException("An account with this email already exists");
        });
    }

    private void ensurePhoneNumberIsAvailable(String phoneNumber) {
        userRepository.findByPhoneNumberManual(phoneNumber).ifPresent(existingUser -> {
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
