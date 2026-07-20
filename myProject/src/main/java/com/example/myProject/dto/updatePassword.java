package com.example.myProject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class updatePassword {

    @NotBlank
    @Size(min = 8, message = "Password must contain at least 8 characters.")
    private String newPassword;

}
