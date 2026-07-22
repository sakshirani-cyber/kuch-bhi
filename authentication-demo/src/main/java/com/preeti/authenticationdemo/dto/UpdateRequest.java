package com.preeti.authenticationdemo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.preeti.authenticationdemo.validation.AtLeastOneFieldRequired;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@AtLeastOneFieldRequired
public class UpdateRequest {

    @NotBlank(message = "Current username is required")
    private String currentUsername;

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    // All fields below are optional: leave blank/null to keep the existing value.
    // Bean Validation constraints below (Pattern, Email) are skipped automatically
    // when the value is null, so they only fire if the user actually provides one.

    @Pattern(regexp = "^[a-zA-Z0-9._]{3,20}$", message = "New username must be 3-20 characters (letters, numbers, dots, underscores only)")
    private String newUsername;

    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,13}$",
            message = "New password must be 8-13 characters and include an uppercase letter, a lowercase letter, a digit, and a special character (@#$%^&+=!)"
    )
    private String newPassword;

    @Email(message = "Please provide a valid new email address")
    private String newEmail;

    @Pattern(regexp = "^[0-9]{10}$", message = "New phone number must be exactly 10 digits")
    private String newPhoneNumber;

}
