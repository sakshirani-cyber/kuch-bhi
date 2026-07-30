package com.example.AuthProject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateUsernameRequest {

    @NotBlank(message = "Current username is mandatory")
    @Size(min = 3, max = 50, message = "Current username must be between 3 and 50 characters")
    private String currentUsername;

    @NotBlank(message = "New username is mandatory")
    @Size(min = 3, max = 50, message = "New username must be between 3 and 50 characters")
    private String newUsername;
}
