package com.example.myProject.service;

import com.example.myProject.dto.LoginRequest;
import com.example.myProject.dto.MessageResponse;
import com.example.myProject.dto.RegisterRequest;
import com.example.myProject.dto.UpdatePasswordRequest;
import com.example.myProject.dto.UpdateUsernameRequest;
import com.example.myProject.dto.UserResponse;
import com.example.myProject.entity.User;
import com.example.myProject.exception.ApiException;
import com.example.myProject.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository repository;

    public AuthService(UserRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public MessageResponse register(RegisterRequest request) {
        try {
            if (repository.existsByEmail(request.getEmail())) {
                throw new ApiException(
                        HttpStatus.CONFLICT,
                        "Registration failed",
                        Map.of("email", "User already exists")
                );
            }

            int age = calculateAge(request.getDateOfBirth());

            repository.insertUser(
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getContactNumber(),
                    request.getDateOfBirth(),
                    age
            );

            return new MessageResponse("User registered successfully!");
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Registration failed",
                    Map.of("error", ex.getMessage())
            );
        }
    }

    public UserResponse login(LoginRequest request) {
        try {
            Optional<User> optionalUser = repository.findByEmail(request.getEmail());
            if (optionalUser.isEmpty()) {
                throw new ApiException(
                        HttpStatus.UNAUTHORIZED,
                        "Login failed",
                        Map.of("email", "User not found")
                );
            }

            User user = optionalUser.get();
            if (!user.getPassword().equals(request.getPassword())) {
                throw new ApiException(
                        HttpStatus.UNAUTHORIZED,
                        "Login failed",
                        Map.of("password", "Incorrect password")
                );
            }

            return UserResponse.from(user);
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Login failed",
                    Map.of("error", ex.getMessage())
            );
        }
    }

    public UserResponse getUser(Long id) {
        try {
            Optional<User> optionalUser = repository.findUserById(id);
            if (optionalUser.isEmpty()) {
                throw new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Unable to fetch user",
                        Map.of("userId", "User not found")
                );
            }

            User user = optionalUser.get();
            user.setAge(calculateAge(user.getDateOfBirth()));
            return UserResponse.from(user);
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to fetch user",
                    Map.of("error", ex.getMessage())
            );
        }
    }

    @Transactional
    public MessageResponse updatePassword(Long id, UpdatePasswordRequest request) {
        try {
            Optional<User> optionalUser = repository.findUserById(id);
            if (optionalUser.isEmpty()) {
                throw new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Password update failed",
                        Map.of("userId", "User not found")
                );
            }

            User user = optionalUser.get();
            if (user.getPassword().equals(request.getNewPassword())) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "Password update failed",
                        Map.of("newPassword", "New password cannot be the same as old password")
                );
            }

            int updated = repository.updatePasswordById(id, request.getNewPassword());
            if (updated == 0) {
                throw new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Password update failed",
                        Map.of("userId", "User not found")
                );
            }

            return new MessageResponse("Password updated successfully!");
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Password update failed",
                    Map.of("error", ex.getMessage())
            );
        }
    }

    @Transactional
    public MessageResponse updateUsername(Long id, UpdateUsernameRequest request) {
        try {
            Optional<User> optionalUser = repository.findUserById(id);
            if (optionalUser.isEmpty()) {
                throw new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Username update failed",
                        Map.of("userId", "User not found")
                );
            }

            User user = optionalUser.get();
            if (user.getUsername().equals(request.getNewUsername())) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "Username update failed",
                        Map.of("newUsername", "New username cannot be the same as old username")
                );
            }

            int updated = repository.updateUsernameById(id, request.getNewUsername());
            if (updated == 0) {
                throw new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Username update failed",
                        Map.of("userId", "User not found")
                );
            }

            return new MessageResponse("Username updated successfully!");
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Username update failed",
                    Map.of("error", ex.getMessage())
            );
        }
    }

    private int calculateAge(LocalDate dateOfBirth) {
        try {
            if (dateOfBirth == null) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "Registration failed",
                        Map.of("dateOfBirth", "Date of birth is required")
                );
            }
            int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
            if (age < 0) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "Registration failed",
                        Map.of("dateOfBirth", "Invalid date of birth")
                );
            }
            return age;
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Registration failed",
                    Map.of("dateOfBirth", "Unable to calculate age: " + ex.getMessage())
            );
        }
    }
}
