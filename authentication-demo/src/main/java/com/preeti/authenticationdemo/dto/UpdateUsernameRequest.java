package com.preeti.authenticationdemo.dto;

import com.preeti.authenticationdemo.validation.ValidationPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUsernameRequest {

    @NotBlank(message = "Current username is required")
    private String currentUsername;

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New username is required")
    @Pattern(regexp = ValidationPatterns.USERNAME_REGEX, message = "New username must be 3-20 characters (letters, numbers, dots, underscores only)")
    private String newUsername;

}
