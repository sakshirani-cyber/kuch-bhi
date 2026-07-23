package com.example.backend.service;

import com.example.backend.dto.request.LoginRequest;
import com.example.backend.dto.request.SignupRequest;
import com.example.backend.dto.request.UpdateProfileRequest;
import com.example.backend.dto.response.AuthResponse;
import com.example.backend.dto.response.UserDto;
import com.example.backend.entity.User;
import com.example.backend.exception.InvalidCredentialsException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.exception.UserAlreadyExistsException;
import com.example.backend.repository.UserRepository;
import com.example.backend.utils.EncryptionUtil;

import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final EncryptionUtil encryptionUtil;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EncryptionUtil encryptionUtil) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.encryptionUtil = encryptionUtil;
	}

	public AuthResponse login(LoginRequest request) {
		String identifier = safeTrim(request != null ? request.getIdentifier() : null);
		String password = request != null ? request.getPassword() : null;

		log.info("Attempting login for identifier: {}", identifier);
		validateIdentifier(identifier);

		Optional<User> userOpt = findByIdentifierCached(identifier);

		if (userOpt.isEmpty()) {
			log.error("User not found with identifier: {}", identifier);
			throw new ResourceNotFoundException("USER_NOT_FOUND");
		}

		User user = userOpt.get();
		if (password == null || !passwordEncoder.matches(password, user.getPassword())) {
			log.error("Invalid password for identifier: {}", identifier);
			throw new InvalidCredentialsException("INVALID_PASSWORD");
		}

		log.info("Login successful for user: {}", user.getUsername());
		return new AuthResponse(true, "Hi there", new UserDto(user, encryptionUtil));
	}

	@CacheEvict(value = "users", allEntries = true)
	public AuthResponse signup(SignupRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Signup request cannot be null");
		}

		String username = safeTrim(request.getUsername());
		String email = safeTrim(request.getEmail());

		log.info("Attempting signup for username: {} and email: {}", username, email);

		if (username != null && userRepository.findByUsername(username).isPresent()) {
			log.error("Username already exists: {}", username);
			throw new UserAlreadyExistsException("Username already exists");
		}
		if (email != null && userRepository.findByEmail(email).isPresent()) {
			log.error("Email already exists: {}", email);
			throw new UserAlreadyExistsException("Email already exists");
		}

		String password = request.getPassword();
		String hashedPassword = password != null ? passwordEncoder.encode(password) : null;

		String rawContactNumber = safeTrim(request.getContactNumber());
		String encryptedContactNumber = rawContactNumber != null ? encryptionUtil.encrypt(rawContactNumber) : null;

		User user = new User(
				username,
				safeTrim(request.getFirstName()),
				safeTrim(request.getLastName()),
				email,
				request.getGender(),
				hashedPassword,
				encryptedContactNumber,
				request.getDob(),
				safeTrim(request.getAddress()),
				safeTrim(request.getCollegeName()),
				safeTrim(request.getSchoolName()),
				safeTrim(request.getCurrentCompany()));

		User savedUser = userRepository.save(user);
		log.info("Signup successful for user: {}", savedUser.getUsername());
		return new AuthResponse(true, "Account created successfully", new UserDto(savedUser, encryptionUtil));
	}

	@CacheEvict(value = "users", allEntries = true)
	public AuthResponse updateProfile(UpdateProfileRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Update profile request cannot be null");
		}

		String identifier = safeTrim(request.getIdentifier());
		String password = request.getPassword();

		log.info("Attempting profile update for identifier: {}", identifier);
		validateIdentifier(identifier);

		Optional<User> userOpt = userRepository.findByIdentifier(identifier);

		if (userOpt.isEmpty()) {
			log.error("User not found with identifier: {}", identifier);
			throw new ResourceNotFoundException("USER_NOT_FOUND");
		}

		User user = userOpt.get();

		if (password == null || !passwordEncoder.matches(password, user.getPassword())) {
			log.error("Invalid password provided during profile update for identifier: {}", identifier);
			throw new InvalidCredentialsException("INVALID_PASSWORD");
		}

		user.setFirstName(safeTrim(request.getFirstName()));
		user.setLastName(safeTrim(request.getLastName()));
		user.setGender(request.getGender());

		String rawContactNumber = safeTrim(request.getContactNumber());
		if (rawContactNumber != null) {
			user.setContactNumber(encryptionUtil.encrypt(rawContactNumber));
		}

		user.setDob(request.getDob());
		user.setAddress(safeTrim(request.getAddress()));
		user.setCollegeName(safeTrim(request.getCollegeName()));
		user.setSchoolName(safeTrim(request.getSchoolName()));
		user.setCurrentCompany(safeTrim(request.getCurrentCompany()));

		User updatedUser = userRepository.save(user);
		log.info("Profile update successful for user: {}", updatedUser.getUsername());

		return new AuthResponse(true, "Profile updated successfully", new UserDto(updatedUser, encryptionUtil));
	}

	@Cacheable(value = "users", key = "#identifier", unless = "#result == null || !#result.isPresent()")
	public Optional<User> findByIdentifierCached(String identifier) {
		return userRepository.findByIdentifier(identifier);
	}

	private void validateIdentifier(String identifier) {
		if (identifier == null || identifier.isEmpty()) {
			throw new IllegalArgumentException("Identifier is required");
		}
		boolean isUsername = identifier.matches("^[a-zA-Z0-9_]{5,14}$");
		boolean isEmail = identifier.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

		if (!isUsername && !isEmail) {
			throw new IllegalArgumentException("Identifier must be a valid username or email address");
		}
	}

	private String safeTrim(String value) {
		return value != null ? value.trim() : null;
	}
}
