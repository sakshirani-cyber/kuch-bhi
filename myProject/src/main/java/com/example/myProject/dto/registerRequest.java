package com.example.myProject.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class registerRequest {
    @NotBlank(message = "Username is Mandatory!")
    @Size(min=3,max = 50)
    private String username;

    @Email(message = "Invalid Input")
    @NotBlank
    private String email;

    @NotBlank
    @Size(min=8, message = "Password must contain at least 8 characters.")
    private String password;

}
