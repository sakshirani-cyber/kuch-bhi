package com.example.backend.service;

// Service to manage user retrieval and update user profile

import com.example.backend.dto.request.UpdateProfileRequest;
import com.example.backend.dto.response.AuthResponse;
import com.example.backend.entity.User;
import com.example.backend.exception.InvalidCredentialsException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.mapper.UserMapper;
import com.example.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UserService {

	private final UserRepository userRepository;
	private final PasswordService passwordService;
	private final EncryptionService encryptionService;
	private final UserMapper userMapper;

	public UserService(UserRepository userRepository, PasswordService passwordService, EncryptionService encryptionService, UserMapper userMapper) {
		this.userRepository = userRepository;
		this.passwordService = passwordService;
		this.encryptionService = encryptionService;
		this.userMapper = userMapper;
	}

	@Transactional
	@CacheEvict(value = "users", key = "#username")
	public AuthResponse updateProfile(String username, UpdateProfileRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Update profile request cannot be null");
		}

		String password = request.getPassword();

		log.info("Attempting profile update");

		User user = findByIdentifierCached(username);

		if (!passwordService.matches(password, user.getPassword())) {
			log.error("Invalid password provided during profile update");
			throw new InvalidCredentialsException("INVALID_PASSWORD");
		}

		user.setFirstName(safeTrim(request.getFirstName()));
		user.setLastName(safeTrim(request.getLastName()));
		user.setGender(request.getGender());

		String rawContactNumber = safeTrim(request.getContactNumber());
		if (rawContactNumber != null) {
			user.setContactNumber(encryptionService.encrypt(rawContactNumber));
		}

		user.setDob(request.getDob());
		user.setAddress(safeTrim(request.getAddress()));
		user.setCollegeName(safeTrim(request.getCollegeName()));
		user.setSchoolName(safeTrim(request.getSchoolName()));
		user.setCurrentCompany(safeTrim(request.getCurrentCompany()));

		User updatedUser = userRepository.save(user);
		log.info("Profile update successful");

		return new AuthResponse(true, "Profile updated successfully", userMapper.toDto(updatedUser));
	}

	@Cacheable(value = "users", key = "#identifier")
	public User findByIdentifierCached(String identifier) {
		log.info("[Redis Cache] Cache MISS (fetching from database) for user identifier: {}", identifier);
		return userRepository.findByIdentifier(identifier)
				.orElseThrow(() -> {
					log.error("User not found with identifier: {}", identifier);
					return new ResourceNotFoundException("USER_NOT_FOUND");
				});
	}

	private String safeTrim(String value) {
		return value != null ? value.trim() : null;
	}
}
