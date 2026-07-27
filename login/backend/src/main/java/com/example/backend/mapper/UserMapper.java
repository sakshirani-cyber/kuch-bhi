package com.example.backend.mapper;

// Component to map between User entities and DTOs

import com.example.backend.dto.request.SignupRequest;
import com.example.backend.dto.response.UserDto;
import com.example.backend.entity.User;
import com.example.backend.service.EncryptionService;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    private final EncryptionService encryptionService;

    public UserMapper(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    
    public User toEntity(SignupRequest request, String hashedPassword, String encryptedContactNumber) {
        if (request == null) {
            throw new IllegalArgumentException("SignupRequest cannot be null");
        }


        return User.builder()
                .username(safeTrim(request.getUsername()))
                .firstName(safeTrim(request.getFirstName()))
                .lastName(safeTrim(request.getLastName()))
                .email(normalizedEmail(request.getEmail()))
                .gender(request.getGender())
                .password(hashedPassword)
                .contactNumber(encryptedContactNumber)
                .dob(request.getDob())
                .address(safeTrim(request.getAddress()))
                .collegeName(safeTrim(request.getCollegeName()))
                .schoolName(safeTrim(request.getSchoolName()))
                .currentCompany(safeTrim(request.getCurrentCompany()))
                .build();
    }

    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        String decryptedContact = user.getContactNumber();
        if (decryptedContact != null) {
            try {
                decryptedContact = encryptionService.decrypt(user.getContactNumber());
            } catch (Exception e) {
                // Return encrypted or fallback on error
                e.printStackTrace();
            }
        }

        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName() != null ? user.getLastName() : "")
                .email(user.getEmail())
                .gender(user.getGender())
                .contactNumber(decryptedContact)
                .dob(user.getDob())
                .age(user.getAge())
                .address(user.getAddress() != null ? user.getAddress() : "")
                .collegeName(user.getCollegeName() != null ? user.getCollegeName() : "")
                .schoolName(user.getSchoolName() != null ? user.getSchoolName() : "")
                .currentCompany(user.getCurrentCompany() != null ? user.getCurrentCompany() : "")
                .build();
    }

    private String safeTrim(String value) {
        return value != null ? value.trim() : null;
    }

	private String normalizedEmail(String email) {
		return email != null ? email.trim().toLowerCase() : null;
	}
}
