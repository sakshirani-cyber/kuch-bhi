package com.preeti.authenticationdemo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.preeti.authenticationdemo.validation.ValidationPatterns;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    @NotBlank(message = "Username is required")
    @Pattern(regexp = ValidationPatterns.USERNAME_REGEX, message = "Username must be 3-20 characters (letters, numbers, dots, underscores only)")
    private String username;

    @NotBlank(message = "Password is required")
    @Pattern(regexp = ValidationPatterns.PASSWORD_REGEX, message = "Password must be 8-13 characters and include an uppercase letter, a lowercase letter, a digit, and a special character (@#$%^&+=!)")
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Pattern(regexp = ValidationPatterns.EMAIL_REGEX, message = "Please provide a valid email address with no surrounding spaces")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = ValidationPatterns.PHONE_REGEX, message = "Phone number must be exactly 10 digits")
    private String phoneNumber;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be a date in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

}
