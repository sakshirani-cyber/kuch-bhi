package com.authentication.AuthProject.service;

import com.authentication.AuthProject.dto.LoginRequest;
import com.authentication.AuthProject.dto.SignupRequest;
import com.authentication.AuthProject.model.User;
import com.authentication.AuthProject.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository repository;

    public AuthService(UserRepository repository) {
        this.repository = repository;
    }

    public String signup(SignupRequest request){

        if (repository.findByEmail(request.getEmail()).isPresent()) {
            return "Email already registered.";
        }

        User user = new User();

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());

        repository.save(user);

        return "Signup Successful";

    }

    public String login(LoginRequest request) {
        User user = repository.findByEmail(request.getEmail()).orElse(null);

        if(user == null) {
            return "User not found";
        }

        if(!user.getPassword().equals(request.getPassword())) {
            return "Invalid Password";
        }

        return "Login Successful";
    }
}
