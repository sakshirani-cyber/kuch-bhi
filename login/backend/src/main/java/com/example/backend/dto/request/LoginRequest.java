package com.example.backend.dto.request;

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
public class LoginRequest {
    @NotBlank(message = "Identifier (username or email) is required")
    @Pattern(
		regexp = "^((?!.*_.*_)[A-Za-z0-9_]{5,14}|[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,})$",
		message = "Identifier must be a valid username or email address"
	)
    private String identifier;

    @NotBlank(message = "Password is required")
    private String password;
}
