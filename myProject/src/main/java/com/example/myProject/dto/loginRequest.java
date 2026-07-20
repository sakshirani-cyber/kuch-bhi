package com.example.myProject.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class loginRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

}
