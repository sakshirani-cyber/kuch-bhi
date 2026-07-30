package com.authentication.AuthProject.service;

import com.authentication.AuthProject.exception.RateLimitExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class RateLimitService {

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final Duration LOGIN_WINDOW = Duration.ofMinutes(1);
    private static final String LOGIN_PREFIX = "login:";

    private final StringRedisTemplate redisTemplate;

    public void checkLoginBlocked(String email) {

        String key = LOGIN_PREFIX + normalizeEmail(email);
        String attempts = redisTemplate.opsForValue().get(key);

        if (attempts == null) {
            return;
        }

        int count = Integer.parseInt(attempts);
        if (count >= MAX_LOGIN_ATTEMPTS) {

            log.warn("Rate limit exceeded for {}", email);
            throw new RateLimitExceededException(
                    "Too many failed login attempts. Please try again after one minute."
            );
        }
    }

    public void recordFailedAttempt(String email) {

        String key = LOGIN_PREFIX + normalizeEmail(email);
        Long count = redisTemplate.opsForValue().increment(key);

        if (count == null) {
            return;
        }

        // Set TTL only once
        if (count == 1) {
            redisTemplate.expire(key, LOGIN_WINDOW);
        }

        log.debug("Failed login attempts for {} : {}", email, count);
    }

    public void resetLoginLimit(String email) {

        redisTemplate.delete(LOGIN_PREFIX + normalizeEmail(email));

        log.debug("Login counter reset for {}", email);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
