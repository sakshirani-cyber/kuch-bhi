package com.authentication.AuthProject.controller;

import com.authentication.AuthProject.dto.request.ChangePasswordRequest;
import com.authentication.AuthProject.dto.request.UpdateProfileRequest;
import com.authentication.AuthProject.dto.response.UserResponse;
import com.authentication.AuthProject.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(
            @PathVariable Long id) {

        UserResponse response = service.getUser(id);

        return ResponseEntity.ok(response);
    }


    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserResponse response = service.updateUser(id, request);

        return ResponseEntity.ok(response);
    }


    @PutMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody  ChangePasswordRequest request) {

        service.changePassword(id, request);

        return ResponseEntity.noContent().build();
    }
}
