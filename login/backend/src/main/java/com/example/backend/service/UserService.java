package com.example.backend.service;

import com.example.backend.dto.AuthResponse;
import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.SignupRequest;
import com.example.backend.dto.UpdateProfileRequest;
import com.example.backend.dto.UserDto;
import com.example.backend.exception.InvalidCredentialsException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.exception.UserAlreadyExistsException;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class UserService {

	private final UserRepository userRepository;

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public AuthResponse login(LoginRequest request) {
		log.info("Attempting login for identifier: {}", request.getIdentifier());
		Optional<User> userOpt = userRepository.findByIdentifier(request.getIdentifier());

		if (userOpt.isEmpty()) {
			log.error("User not found with identifier: {}", request.getIdentifier());
			throw new ResourceNotFoundException("USER_NOT_FOUND");
		}

		User user = userOpt.get();
		if (!user.getPassword().equals(request.getPassword())) {
			log.error("Invalid password for identifier: {}", request.getIdentifier());
			throw new InvalidCredentialsException("INVALID_PASSWORD");
		}

		log.info("Login successful for user: {}", user.getUsername());
		return new AuthResponse(true, "Hi there", new UserDto(user));
	}

	public AuthResponse signup(SignupRequest request) {
		log.info("Attempting signup for username: {} and email: {}", request.getUsername(), request.getEmail());
		if (userRepository.existsByUsername(request.getUsername())) {
			log.error("Username already exists: {}", request.getUsername());
			throw new UserAlreadyExistsException("Username already exists");
		}
		if (userRepository.existsByEmail(request.getEmail())) {
			log.error("Email already exists: {}", request.getEmail());
			throw new UserAlreadyExistsException("Email already exists");
		}

		User user = new User(
				request.getUsername(),
				request.getFirstName(),
				request.getLastName(),
				request.getEmail(),
				request.getGender(),
				request.getPassword(),
				request.getContactNumber(),
				request.getDob(),
				request.getAddress(),
				request.getCollegeName(),
				request.getSchoolName(),
				request.getCurrentCompany());

		User savedUser = userRepository.save(user);
		log.info("Signup successful for user: {}", savedUser.getUsername());
		return new AuthResponse(true, "Account created successfully", new UserDto(savedUser));
	}

	public AuthResponse updateProfile(UpdateProfileRequest request) {
		log.info("Attempting profile update for identifier: {}", request.getIdentifier());
		Optional<User> userOpt = userRepository.findByIdentifier(request.getIdentifier());

		if (userOpt.isEmpty()) {
			log.error("User not found with identifier: {}", request.getIdentifier());
			throw new ResourceNotFoundException("USER_NOT_FOUND");
		}

		User user = userOpt.get();

		if (!user.getPassword().equals(request.getPassword())) {
			log.error("Invalid password provided during profile update for identifier: {}", request.getIdentifier());
			throw new InvalidCredentialsException("INVALID_PASSWORD");
		}

		user.setFirstName(request.getFirstName());
		user.setLastName(request.getLastName());
		user.setGender(request.getGender());
		user.setContactNumber(request.getContactNumber());
		user.setDob(request.getDob());
		user.setAddress(request.getAddress());
		user.setCollegeName(request.getCollegeName());
		user.setSchoolName(request.getSchoolName());
		user.setCurrentCompany(request.getCurrentCompany());

		User updatedUser = userRepository.save(user);
		log.info("Profile update successful for user: {}", updatedUser.getUsername());

		return new AuthResponse(true, "Profile updated successfully", new UserDto(updatedUser));
	}
}
