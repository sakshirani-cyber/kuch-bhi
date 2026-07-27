package com.example.backend.dto.response;

// Response DTO returned on successful login or signup, including JWT access token

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {
    private boolean success;
    private String message;
    private String accessToken;
    private UserDto user;

    public AuthResponse(boolean success, String message, UserDto user) {
        this.success = success;
        this.message = message;
        this.user = user;
    }

    public AuthResponse(boolean success, String message, String accessToken, UserDto user) {
        this.success = success;
        this.message = message;
        this.accessToken = accessToken;
        this.user = user;
    }
}
