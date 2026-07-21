package com.example.backend.dto;

import com.example.backend.model.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class UserDto {
	private Long id;
	private String username;
	private String firstName;
	private String lastName;
	private String email;
	private String gender;
	private String contactNumber;
	private LocalDate dob;
	private Integer age;
	private String address;
	private String collegeName;
	private String schoolName;
	private String currentCompany;

	public UserDto(User user) {
		this.id = user.getId();
		this.username = user.getUsername();
		this.firstName = user.getFirstName() != null ? user.getFirstName() : "";
		this.lastName = user.getLastName() != null ? user.getLastName() : "";
		this.email = user.getEmail();
		this.gender = user.getGender() != null ? user.getGender() : "";
		this.contactNumber = user.getContactNumber() != null ? user.getContactNumber() : "";
		this.dob = user.getDob();
		this.age = user.getAge();
		this.address = user.getAddress() != null ? user.getAddress() : "";
		this.collegeName = user.getCollegeName() != null ? user.getCollegeName() : "";
		this.schoolName = user.getSchoolName() != null ? user.getSchoolName() : "";
		this.currentCompany = user.getCurrentCompany() != null ? user.getCurrentCompany() : "";
	}
}
