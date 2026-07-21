package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Identifier (username or email) is required")
    private String identifier;

    @NotBlank(message = "Password is required")
    private String password;
}
