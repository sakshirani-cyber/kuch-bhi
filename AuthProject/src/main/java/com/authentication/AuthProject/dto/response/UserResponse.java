package com.authentication.AuthProject.dto.response;

import com.authentication.AuthProject.enums.Gender;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private LocalDate dob;
    private int age;
    private Gender gender;
    private String email;
    private String phoneNumber;

}
