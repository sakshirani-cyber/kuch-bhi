package com.example.backend.model;

import java.time.LocalDate;
import jakarta.persistence.*;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Period;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false)
	private String username;

	@Column(nullable = false)
	private String firstName;

	private String lastName;

	@Column(unique = true, nullable = false)
	@jakarta.validation.constraints.Email(message = "Email should be valid")
	private String email;

	@Column(nullable = false)
	private String gender;

	@Column(nullable = false)
	@Size(min = 8, message = "Password must be at least 8 characters long")
	private String password;

	@Column(nullable = false)
	@PastOrPresent(message = "DOB can't be a future date")
	private LocalDate dob;

	@Column(nullable = false)
	@Size(max = 10, message = "Contact number should not be longer than 10 digits")
	private String contactNumber;

	private String address;

	private String collegeName;

	private String schoolName;

	private String currentCompany;

	// add validations
	public User(String username, String firstName, String lastName, String email, String gender, String password,
			String contactNumber, LocalDate dob, String address, String collegeName, String schoolName,
			String currentCompany) {
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.gender = gender;
		this.password = password;
		this.contactNumber = contactNumber;
		this.dob = dob;
		this.address = address;
		this.collegeName = collegeName;
		this.schoolName = schoolName;
		this.currentCompany = currentCompany;
	}

	// check for wrapper -> INTEGER
	public Integer getAge() {
		if (this.dob == null) {
			return 0;
		}
		return Period.between(this.dob, LocalDate.now()).getYears();
	}

}
