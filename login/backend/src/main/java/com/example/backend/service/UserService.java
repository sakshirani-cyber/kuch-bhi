package com.example.backend.service;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

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

    public User signup(String username, String email, String password) {
        if (repo.existsByUsername(username)) {
            throw new IllegalStateException("Username already exists");
        }
        if (repo.existsByEmail(email)) {
            throw new IllegalStateException("Email already exists");
        }

        return repo.save(new User(username, email, password));
    }
}
