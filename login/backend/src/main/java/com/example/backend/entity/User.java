package com.example.backend.entity;

import com.example.backend.enums.Gender;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Period;

// Entity representing a user in the database

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

	@Builder.Default
	@Column(nullable = false)
	private boolean isVerified = false;

	@JsonIgnore
	public Integer getAge() {
		if (this.dob == null) {
			return 0;
		}
		return Period.between(this.dob, LocalDate.now()).getYears();
	}

}
