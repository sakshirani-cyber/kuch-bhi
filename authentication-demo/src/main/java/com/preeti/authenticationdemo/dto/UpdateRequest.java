package com.preeti.authenticationdemo.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateRequest(

        @NotBlank
        String currentUsername,

        @NotBlank
        String currentPassword,

        String newUsername,

        String newPassword

) {
}
