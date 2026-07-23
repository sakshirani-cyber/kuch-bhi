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
public class UpdatePhoneNumberRequest {

    @NotBlank(message = "Current username is required")
    private String currentUsername;

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New phone number is required")
    @Pattern(regexp = ValidationPatterns.PHONE_REGEX, message = "New phone number must be exactly 10 digits")
    private String newPhoneNumber;

}
