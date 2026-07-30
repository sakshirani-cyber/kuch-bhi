package com.preeti.authenticationdemo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimitingServiceTest {

    private RateLimitingService rateLimitingService;

    @BeforeEach
    void setUp() {
        rateLimitingService = new RateLimitingService();
    }

    @Test
    @DisplayName("Rate Limit: Login allows up to 5 requests per minute, then blocks 6th")
    void tryConsume_LoginEndpoint_EnforcesLimitOf5() {
        String ip = "192.168.1.100";
        String uri = "/api/v1/auth/login";

        for (int i = 0; i < 5; i++) {
            RateLimitingService.RateLimitResult result = rateLimitingService.tryConsume(ip, uri);
            assertTrue(result.isConsumed(), "Request " + (i + 1) + " should be allowed");
        }

        RateLimitingService.RateLimitResult blockedResult = rateLimitingService.tryConsume(ip, uri);
        assertFalse(blockedResult.isConsumed(), "6th request should be blocked");
        assertEquals(0, blockedResult.getRemainingTokens());
        assertTrue(blockedResult.getWaitForRefillSeconds() > 0);
    }

    @Test
    @DisplayName("Rate Limit: Signup allows up to 3 requests per minute, then blocks 4th")
    void tryConsume_SignupEndpoint_EnforcesLimitOf3() {
        String ip = "192.168.1.101";
        String uri = "/api/v1/auth/signup";

        for (int i = 0; i < 3; i++) {
            RateLimitingService.RateLimitResult result = rateLimitingService.tryConsume(ip, uri);
            assertTrue(result.isConsumed());
        }

        RateLimitingService.RateLimitResult blockedResult = rateLimitingService.tryConsume(ip, uri);
        assertFalse(blockedResult.isConsumed());
    }

    @Test
    @DisplayName("Rate Limit: Different IPs have independent rate limit buckets")
    void tryConsume_DifferentIPs_HaveIndependentBuckets() {
        String ip1 = "10.0.0.1";
        String ip2 = "10.0.0.2";
        String uri = "/api/v1/auth/signup";

        for (int i = 0; i < 3; i++) {
            assertTrue(rateLimitingService.tryConsume(ip1, uri).isConsumed());
        }
        assertFalse(rateLimitingService.tryConsume(ip1, uri).isConsumed());

        assertTrue(rateLimitingService.tryConsume(ip2, uri).isConsumed());
    }
}
