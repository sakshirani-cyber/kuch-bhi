package com.preeti.authenticationdemo.dto;

import com.preeti.authenticationdemo.validation.ValidationPatterns;
import jakarta.validation.constraints.Email;
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
public class UpdateEmailRequest {

    @NotBlank(message = "Current username is required")
    private String currentUsername;

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New email is required")
    @Email(message = "Please provide a valid email address")
    @Pattern(regexp = ValidationPatterns.EMAIL_REGEX, message = "Please provide a valid email address with no surrounding spaces")
    private String newEmail;

}
