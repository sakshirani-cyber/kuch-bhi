package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProfileRequest {
	@NotBlank(message = "Identifier (username or email) is required")
	private String identifier;

	@NotBlank(message = "Password is required to confirm update")
	private String password;

	@NotBlank(message = "First name is required")
	private String firstName;

	private String lastName;

	@NotBlank(message = "Gender is required")
	private String gender;

	@NotBlank(message = "Contact number is required")
	private String contactNumber;

	private LocalDate dob;
	private String address;
	private String collegeName;
	private String schoolName;
	private String currentCompany;
}
