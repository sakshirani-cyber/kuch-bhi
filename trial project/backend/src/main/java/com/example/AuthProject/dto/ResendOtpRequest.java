package com.example.AuthProject.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ResendOtpRequest {

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Invalid email format")
    private String email;
}
