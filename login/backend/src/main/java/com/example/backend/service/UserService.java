package com.example.backend.service;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class UserService {

    private UserRepository repo;

	public UserService(UserRepository repo) {
		this.repo = repo;
	}
    public User login(String identifier, String password) {
        Optional<User> userOpt = repo.findByUsernameOrEmail(identifier, identifier);

        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("USER_NOT_FOUND");
        }

        if (!userOpt.get().getPassword().equals(password)) {
            throw new IllegalArgumentException("INVALID_PASSWORD");
        }

        return userOpt.get();
    }

    public User signup(String username, String firstName, String lastName, String email, String gender, String password, String contactNumber, LocalDate dob, String address, String collegeName, String schoolName, String currentcompany) {
        if (repo.existsByUsername(username)) {
            throw new IllegalStateException("Username already exists");
        }
        if (repo.existsByEmail(email)) {
            throw new IllegalStateException("Email already exists");
        }

        return repo.save(new User(username, firstName, lastName, email, gender, password, contactNumber, dob, address, collegeName, schoolName, currentcompany));
    }

    public User updateProfile(String identifier, String firstName, String lastName, String gender, String password, String contactNumber, LocalDate dob, String address, String collegeName, String schoolName, String currentcompany) {
        Optional<User> userOpt = repo.findByUsernameOrEmail(identifier, identifier);

        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("USER_NOT_FOUND");
        }

        User user = userOpt.get();

        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("INVALID_PASSWORD");
        }

		// auto generated getter and setters
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setGender(gender);
        user.setContactNumber(contactNumber);
        user.setDob(dob);
        user.setAddress(address);
        user.setCollegeName(collegeName);
        user.setSchoolName(schoolName);
        user.setCurrentcompany(currentcompany);

        return repo.save(user);
    }
}
