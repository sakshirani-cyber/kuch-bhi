package com.example.myProject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DeleteUserRequest {

    @NotBlank(message = "Password is mandatory")
    @Size(min = 8, max = 20, message = "Password must be 8-20 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,20}$",
            message = "Password must include uppercase, lowercase, number and special character"
    )
    private String password;
}
