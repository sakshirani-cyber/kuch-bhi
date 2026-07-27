package com.authentication.AuthProject.service;

import com.authentication.AuthProject.dto.request.ChangePasswordRequest;
import com.authentication.AuthProject.dto.request.UpdateProfileRequest;
import com.authentication.AuthProject.dto.response.UserResponse;
import com.authentication.AuthProject.entity.User;
import com.authentication.AuthProject.exception.BadRequestException;
import com.authentication.AuthProject.exception.DuplicateResourceException;
import com.authentication.AuthProject.exception.InvalidCredentialsException;
import com.authentication.AuthProject.exception.ResourceNotFoundException;
import com.authentication.AuthProject.repository.UserRepository;
import com.authentication.AuthProject.util.EncryptionService;
import com.authentication.AuthProject.util.PhoneHashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final EncryptionService encryptionService;
    private final PhoneHashService phoneHashService;

    public UserResponse getUser(Long id) {
        log.debug("Attempting to retrieve user details for ID: {}", id);
        User user = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User retrieval failed: User ID {} not found", id);
                    return new ResourceNotFoundException("User not found.");
                });

        return toResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateProfileRequest request) {
        log.debug("Attempting to update profile for user ID: {}", id);
        User user = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User update failed: User ID {} not found", id);
                    return new ResourceNotFoundException("User not found.");
                });

        String newPhone = request.getPhoneNumber();
        String newPhoneHash = phoneHashService.hash(newPhone);

        if (!user.getPhoneNumberHash().equals(newPhoneHash)
                && repository.existsByPhoneNumberHash(newPhoneHash)) {
            log.warn("User update failed: Phone number is already registered for another user");
            throw new DuplicateResourceException(
                    "Phone number already registered.");
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setDob(request.getDob());
        user.setGender(request.getGender());
        user.setPhoneNumber(encryptionService.encrypt(newPhone));
        user.setPhoneNumberHash(newPhoneHash);

        log.info("Successfully updated user profile for ID: {}", id);
        return toResponse(user);
    }

    @Transactional
    public void changePassword(Long id, ChangePasswordRequest request) {
        log.debug("Attempting to change password for user ID: {}", id);
        User user = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Password change failed: User ID {} not found", id);
                    return new ResourceNotFoundException("User not found.");
                });

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            log.warn("Password change failed: Current password is incorrect for user ID {}", id);
            throw new InvalidCredentialsException(
                    "Current password is incorrect.");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            log.warn("Password change failed: New password and confirm password do not match for user ID {}", id);
            throw new BadRequestException(
                    "New password and confirm password do not match.");
        }

        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            log.warn("Password change failed: New password must be different from current password for user ID {}", id);
            throw new BadRequestException(
                    "New password must be different from current password.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        log.info("Successfully changed password for user ID: {}", id);
    }

    private UserResponse toResponse(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(buildFullName(user.getFirstName(), user.getLastName()))
                .dob(user.getDob())
                .age(Period.between(user.getDob(), LocalDate.now()).getYears())
                .gender(user.getGender())
                .email(user.getEmail())
                .phoneNumber(encryptionService.decrypt(user.getPhoneNumber()))
                .build();
    }


    private String buildFullName(String firstName, String lastName) {
    if (lastName == null || lastName.isBlank()) {
        return firstName;
    }
    return firstName + " " + lastName;
    }
}
