package com.authentication.AuthProject.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResendOtpRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    private String email;
}
