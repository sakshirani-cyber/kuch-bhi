package com.example.backend.dto.response;

import com.example.backend.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

// Data Transfer Object representing user data sent to the client

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
	private Long id;
	private String username;
	private String firstName;
	private String lastName;
	private String email;
	private Gender gender;
	private String contactNumber;
	private LocalDate dob;
	private Integer age;
	private String address;
	private String collegeName;
	private String schoolName;
	private String currentCompany;
}
