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
public class UpdatePasswordRequest {

    @NotBlank(message = "Current username is required")
    private String currentUsername;

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Pattern(regexp = ValidationPatterns.PASSWORD_REGEX, message = "New password must be 8-13 characters and include an uppercase letter, a lowercase letter, a digit, and a special character (@#$%^&+=!)")
    private String newPassword;

}
