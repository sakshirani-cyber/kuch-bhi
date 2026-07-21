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
import com.authentication.AuthProject.util.AgeCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository repository;

    public UserResponse getUser(Long id) {

        User user = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found."));

        return toResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateProfileRequest request) {

        User user = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found."));

        String newPhone = request.getPhoneNumber();

        if (!user.getPhoneNumber().equals(newPhone)
                && repository.existsByPhoneNumber(newPhone)) {

            throw new DuplicateResourceException(
                    "Phone number already registered.");
        }

        user.setPhoneNumber(newPhone);

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setDob(request.getDob());
        user.setGender(request.getGender());
        user.setPhoneNumber(newPhone);

        return toResponse(user);
    }

    @Transactional
    public void changePassword(Long id, ChangePasswordRequest request) {

        User user = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found."));

        if (!user.getPassword().equals(request.getCurrentPassword())) {
            throw new InvalidCredentialsException(
                    "Current password is incorrect.");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException(
                    "New password and confirm password do not match.");
        }

        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new BadRequestException(
                    "New password must be different from current password.");
        }
        //use sql query for updation

        user.setPassword(request.getNewPassword());
    }

    private UserResponse toResponse(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .dob(user.getDob())
                .age(AgeCalculator.calculate(user.getDob()))
                .gender(user.getGender())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }
}
