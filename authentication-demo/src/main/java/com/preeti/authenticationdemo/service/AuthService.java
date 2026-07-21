package com.preeti.authenticationdemo.service;

import com.preeti.authenticationdemo.dto.LoginRequest;
import com.preeti.authenticationdemo.dto.SignupRequest;
import com.preeti.authenticationdemo.dto.UpdateRequest;
import com.preeti.authenticationdemo.model.User;
import com.preeti.authenticationdemo.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String signup(SignupRequest request) {

        userRepository.findByUsername(request.username()).ifPresent(u -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        });

        String encodedPassword =
                passwordEncoder.encode(request.password());

        User user = new User(
                request.username(),
                encodedPassword
        );

        userRepository.save(user);

        return "User Registered Successfully";
    }

    public String login(LoginRequest request) {

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        return "Login Successful";
    }

    public String updateCredentials(UpdateRequest request) {

        User user = userRepository.findByUsername(request.currentUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        boolean wantsUsernameChange = request.newUsername() != null
                && !request.newUsername().isBlank()
                && !request.newUsername().equals(user.getUsername());

        if (wantsUsernameChange) {
            userRepository.findByUsername(request.newUsername()).ifPresent(u -> {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "That username is already taken");
            });
            user.setUsername(request.newUsername());
        }

        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.newPassword()));
        }

        userRepository.save(user);

        return "Profile updated successfully";
    }

}
