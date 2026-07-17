package com.example.backend.model;

import java.time.LocalDate;
import jakarta.persistence.*;
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

	@Column(unique = true, nullable = false)
    private String firstName;

    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

	@Column(nullable = false)
	private String gender;

    @Column(nullable = false)
    private String password;

	@Column(nullable = false)
	private LocalDate dob;

	@Column(nullable = false)
	private String contactNumber;

	private String address;

	private String collegeName;

	private String schoolName;

	private String currentcompany;


    public User(String username, String firstName, String lastName, String email, String gender, String password, String contactNumber, LocalDate dob, String address, String collegeName, String schoolName, String currentcompany) {
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
        this.currentcompany = currentcompany;
    }

    public int getAge() {
        if (this.dob == null) {
            return 0;
        }
        return Period.between(this.dob, LocalDate.now()).getYears();
    }

}
