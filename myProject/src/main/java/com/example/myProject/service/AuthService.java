package com.example.myProject.service;

import com.example.myProject.dto.UserResponse;
import com.example.myProject.dto.loginRequest;
import com.example.myProject.dto.registerRequest;
import com.example.myProject.dto.updatePassword;
import com.example.myProject.dto.updateUsername;
import com.example.myProject.entity.User;
import com.example.myProject.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UserRepository repository;

    public AuthService(UserRepository repository) {
        this.repository = repository;
    }

    public UserResponse register(registerRequest request) {
        if (repository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already Exists.");
        }
        User user = new User(
                request.getUsername(),
                request.getEmail(),
                request.getPassword()
        );
        repository.save(user);
        return UserResponse.from(user);
    }

    public UserResponse login(loginRequest request) {
        User user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not Found."));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect Password.");
        }

        return UserResponse.from(user);
    }

    public UserResponse getUser(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
        return UserResponse.from(user);
    }

    public UserResponse updatePassword(Long id, updatePassword request) {
        User user = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

        if (user.getPassword().equals(request.getNewPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New Password cannot be the same as Old Password");
        }

        user.setPassword(request.getNewPassword());
        repository.save(user);
        return UserResponse.from(user);
    }

    public UserResponse updateUsername(Long id, updateUsername request) {
        User user = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

        user.setUsername(request.getNewUsername());
        repository.save(user);
        return UserResponse.from(user);
    }
}
