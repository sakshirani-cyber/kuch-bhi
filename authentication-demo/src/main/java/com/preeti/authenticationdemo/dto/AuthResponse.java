package com.preeti.authenticationdemo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String tokenType = "Bearer";
    private String username;
    private String email;

    public AuthResponse(String accessToken, String username, String email) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.username = username;
        this.email = email;
    }

}
