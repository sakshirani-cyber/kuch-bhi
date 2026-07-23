package com.example.backend.dto.request;

import com.example.backend.enums.Gender;
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
public class UpdateProfileRequest {

    @NotBlank(message = "Username or email is required")
    private String identifier;

    @NotBlank(message = "Password is required to confirm update")
    private String password;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Pattern(regexp = "^[A-Za-zÀ-ÿ' -]+$", message = "First name must contain only letters and spaces")
    private String firstName;

    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    @Pattern(regexp = "^[A-Za-zÀ-ÿ' -]+$", message = "Last name must contain only letters and spaces")
    private String lastName;

    @NotNull(message = "Gender is required")
    private Gender gender;

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
