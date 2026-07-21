package com.authentication.AuthProject.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangePasswordRequest {

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 20,
            message = "New password must be between 8 and 20 characters")
    private String newPassword;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;


    /*One thing Bean Validation cannot do

    It can verify:

    ✅ Password is not blank
    ✅ Password length
    ✅ Null values

    But it cannot verify:

    newPassword == confirmPassword

    That is business logic, so it belongs in the service layer.


    Request
        │
                ▼
    Bean Validation
        │
                ▼
    Service
        │
                ├── Current password correct?
            │
            ├── New != Current?
            │
            ├── New == Confirm?
            │
            ▼
            Repository.save()
            */
}
