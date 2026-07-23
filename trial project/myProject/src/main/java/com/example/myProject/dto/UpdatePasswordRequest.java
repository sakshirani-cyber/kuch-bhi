package com.example.myProject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdatePasswordRequest {

    @NotBlank(message = "New password is mandatory")
    @Size(min = 8, message = "Password must contain at least 8 characters")
    private String newPassword;
}
