package com.example.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
public class RedisRateLimiterService {

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int MAX_OTP_REQUESTS = 3;
    private static final Duration WINDOW_DURATION = Duration.ofSeconds(60);

    private static final String LOGIN_KEY_PREFIX = "rate_limit:login:";
    private static final String OTP_KEY_PREFIX = "rate_limit:otp:";

    private final StringRedisTemplate redisTemplate;

    public RedisRateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isLoginRateLimited(String accountKey) {
        String key = LOGIN_KEY_PREFIX + normalize(accountKey);
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return false;
        }
        try {
            int count = Integer.parseInt(value);
            return count >= MAX_LOGIN_ATTEMPTS;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void recordFailedLoginAttempt(String accountKey) {
        String key = LOGIN_KEY_PREFIX + normalize(accountKey);
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, WINDOW_DURATION);
        }
        log.warn("[Redis RateLimiter] Failed login attempt for account key: {}. Current count: {}", accountKey, count);
    }

    public void resetLoginAttempts(String accountKey) {
        String key = LOGIN_KEY_PREFIX + normalize(accountKey);
        redisTemplate.delete(key);
        log.info("[Redis RateLimiter] Reset login attempt counter for account key: {}", accountKey);
    }

    public boolean isOtpRateLimited(String email) {
        String key = OTP_KEY_PREFIX + normalize(email);
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return false;
        }
        try {
            int count = Integer.parseInt(value);
            return count >= MAX_OTP_REQUESTS;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void recordOtpRequest(String email) {
        String key = OTP_KEY_PREFIX + normalize(email);
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, WINDOW_DURATION);
        }
        log.info("[Redis RateLimiter] OTP request logged for email: {}. Current count: {}", email, count);
    }

    private String normalize(String value) {
        return value != null ? value.trim().toLowerCase() : "";
    }
}
