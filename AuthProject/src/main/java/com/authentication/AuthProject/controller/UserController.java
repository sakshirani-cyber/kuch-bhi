package com.authentication.AuthProject.controller;

import com.authentication.AuthProject.dto.request.ChangePasswordRequest;
import com.authentication.AuthProject.dto.request.UpdateProfileRequest;
import com.authentication.AuthProject.dto.response.UserResponse;
import com.authentication.AuthProject.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(
            @PathVariable Long id) {

        log.info("Received request to fetch profile for user ID: {}", id);
        UserResponse response = service.getUser(id);
        log.info("Successfully fetched profile for user ID: {}", id);

        return ResponseEntity.ok(response);
    }


    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProfileRequest request) {
        log.info("Received request to update profile for user ID: {}", id);
        UserResponse response = service.updateUser(id, request);
        log.info("Successfully updated profile for user ID: {}", id);

        return ResponseEntity.ok(response);
    }


    @PutMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody  ChangePasswordRequest request) {

        log.info("Received request to change password for user ID: {}", id);
        service.changePassword(id, request);
        log.info("Successfully changed password for user ID: {}", id);

        return ResponseEntity.noContent().build();
    }
}
