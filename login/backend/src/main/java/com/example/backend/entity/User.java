package com.example.backend.entity;

import com.example.backend.enums.Gender;
import java.time.LocalDate;
import jakarta.persistence.*;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Period;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
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

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Gender gender;

	@Column(nullable = false)
	@Size(min = 8, message = "Password must be at least 8 characters long")
	private String password;

	@Column(nullable = false)
	@PastOrPresent(message = "DOB can't be a future date")
	private LocalDate dob;

	@Column(nullable = false)
	private String contactNumber;

	private String address;

	private String collegeName;

	private String schoolName;

	private String currentCompany;

	public User(String username, String firstName, String lastName, String email, Gender gender, String password, String contactNumber, LocalDate dob, String address, String collegeName, String schoolName, String currentCompany) {
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

	public Integer getAge() {
		if (this.dob == null) {
			return 0;
		}
		return Period.between(this.dob, LocalDate.now()).getYears();
	}

}
