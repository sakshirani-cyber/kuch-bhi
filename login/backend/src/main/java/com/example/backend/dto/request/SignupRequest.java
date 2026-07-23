package com.example.backend.dto.request;

import com.example.backend.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Size(min = 5, max = 14, message = "Username must be between 5 and 14 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]{5,14}$", message = "Username can only contain alphanumeric characters and underscores")
    private String username;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Pattern(regexp = "^[A-Za-zÀ-ÿ' -]+$", message = "First name must contain only letters and spaces")
    private String firstName;

    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    @Pattern(regexp = "^[A-Za-zÀ-ÿ' -]+$", message = "Last name must contain only letters and spaces")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Email format is invalid")
    private String email;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 24, message = "Password must be between 8 and 24 characters")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).*$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
    private String password;

    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Contact number must be 10 digits")
    private String contactNumber;

    @NotNull(message = "Date of birth is required")
    @PastOrPresent(message = "Date of birth cannot be in the future")
    private LocalDate dob;

    @Size(max = 100, message = "Address cannot exceed 100 characters")
    private String address;

    @Size(max = 100, message = "College name cannot exceed 100 characters")
    private String collegeName;

    @Size(max = 100, message = "School name cannot exceed 100 characters")
    private String schoolName;

    @Size(max = 100, message = "Current company name cannot exceed 100 characters")
    private String currentCompany;
}
