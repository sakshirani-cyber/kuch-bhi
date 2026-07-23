package com.example.backend.dto.response;

import com.example.backend.entity.User;
import com.example.backend.enums.Gender;
import com.example.backend.utils.EncryptionUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class UserDto {
	private Long id;
	private String username;
	private String firstName;
	private String lastName;
	private String email;
	private Gender gender;
	private String contactNumber;
	private LocalDate dob;
	private Integer age;
	private String address;
	private String collegeName;
	private String schoolName;
	private String currentCompany;

	public UserDto(User user) {
		this(user, null);
	}

	public UserDto(User user, EncryptionUtil encryptionUtil) {
		if (user == null) {
			return;
		}
		this.id = user.getId();
		this.username = user.getUsername();
		this.firstName = user.getFirstName();
		this.lastName = user.getLastName() != null ? user.getLastName() : "";
		this.email = user.getEmail();
		this.gender = user.getGender();
		if (user.getContactNumber() != null && encryptionUtil != null) {
			try {
				this.contactNumber = encryptionUtil.decrypt(user.getContactNumber());
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			this.contactNumber = user.getContactNumber();
		}
		this.dob = user.getDob();
		this.age = user.getAge();
		this.address = user.getAddress() != null ? user.getAddress() : "";
		this.collegeName = user.getCollegeName() != null ? user.getCollegeName() : "";
		this.schoolName = user.getSchoolName() != null ? user.getSchoolName() : "";
		this.currentCompany = user.getCurrentCompany() != null ? user.getCurrentCompany() : "";
	}
}
