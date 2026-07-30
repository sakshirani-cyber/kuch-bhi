package com.preeti.authenticationdemo.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = "9a8b7c6d5e4f3a2b1c0d9e8f7a6b5c4d3e2f1a0b9c8d7e6f5a4b3c2d1e0f9a8b";
    private static final long EXPIRATION_MS = 3600000; // 1 hour

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION_MS);
    }

    @Test
    @DisplayName("JWT: Generate and validate token successfully")
    void generateAndValidateToken_Success() {
        String token = jwtService.generateToken("TestUser", "test@example.com");

        assertNotNull(token);
        assertTrue(jwtService.validateToken(token));
        assertEquals("TestUser", jwtService.extractUsername(token));
        assertEquals("test@example.com", jwtService.extractEmail(token));
    }

    @Test
    @DisplayName("JWT: Invalid or malformed token fails validation")
    void validateToken_MalformedToken_ReturnsFalse() {
        assertFalse(jwtService.validateToken("invalid-malformed-token-string"));
    }

    @Test
    @DisplayName("JWT: Expired token fails validation")
    void validateToken_ExpiredToken_ReturnsFalse() {
        JwtService shortLivedJwtService = new JwtService(SECRET, -1000); // Expired 1 second ago
        String expiredToken = shortLivedJwtService.generateToken("ExpiredUser", "expired@example.com");

        assertFalse(shortLivedJwtService.validateToken(expiredToken));
    }
}
