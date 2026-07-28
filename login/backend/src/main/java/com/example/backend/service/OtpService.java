package com.example.backend.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class OtpService {

    private static final int MAX_ATTEMPTS = 3;
    private static final int OTP_EXPIRE_MINUTES = 5;
    private static final int VERIFIED_EMAIL_EXPIRE_MINUTES = 15;

    // cache for OTP : Key = email, Value = OTP string, 5min TTL
    private final Cache<String, String> otpCache = Caffeine.newBuilder()
            .expireAfterWrite(OTP_EXPIRE_MINUTES, TimeUnit.MINUTES)
            .build();

    // cache for no_of_attempt: Key = email, Value = int attempt count, 5min TTL, max retries = 3(for same otp)
    private final Cache<String, Integer> attemptCache = Caffeine.newBuilder()
            .expireAfterWrite(OTP_EXPIRE_MINUTES, TimeUnit.MINUTES)
            .build();

    // cache for Verified Emails (before final signup completion): Key = email, Value = Boolean
    private final Cache<String, Boolean> verifiedEmailCache = Caffeine.newBuilder()
            .expireAfterWrite(VERIFIED_EMAIL_EXPIRE_MINUTES, TimeUnit.MINUTES)
            .build();

    private final SecureRandom secureRandom = new SecureRandom();

    public String generateAndStoreOtp(String email) {
        String normalizedEmail = normalizeEmail(email);
        String otp = String.format("%06d", secureRandom.nextInt(1000000));
        otpCache.put(normalizedEmail, otp);
        attemptCache.put(normalizedEmail, 0);
        log.info("Generated new OTP for {}", normalizedEmail);
        return otp;
    }

    public boolean isOtpExpired(String email) {
        String normalizedEmail = normalizeEmail(email);
        return otpCache.getIfPresent(normalizedEmail) == null;
    }

    public int getRemainingAttempts(String email) {
        String normalizedEmail = normalizeEmail(email);
        Integer attempts = attemptCache.getIfPresent(normalizedEmail);
        if (attempts == null) {
            attempts = 0;
        }
        return Math.max(0, MAX_ATTEMPTS - attempts);
    }

    public boolean isMaxAttemptsExceeded(String email) {
        return getRemainingAttempts(email) <= 0;
    }

    public boolean validateOtp(String email, String inputOtp) {
        String normalizedEmail = normalizeEmail(email);
        String cachedOtp = otpCache.getIfPresent(normalizedEmail);

        if (cachedOtp == null) {
            log.warn("OTP validation failed: No active OTP found for {}", normalizedEmail);
            return false;
        }

        if (isMaxAttemptsExceeded(normalizedEmail)) {
            log.warn("OTP validation blocked: Max attempts exceeded for {}", normalizedEmail);
            return false;
        }

        if (cachedOtp.equals(inputOtp)) {
            log.info("OTP verification successful for {}", normalizedEmail);
            clearOtp(normalizedEmail);
            markEmailAsVerified(normalizedEmail);
            return true;
        } else {
            Integer attempts = attemptCache.getIfPresent(normalizedEmail);
            int currentAttempts = (attempts != null ? attempts : 0) + 1;
            attemptCache.put(normalizedEmail, currentAttempts);
            log.warn("Invalid OTP entered for {}. Attempt {}/{}", normalizedEmail, currentAttempts, MAX_ATTEMPTS);
            return false;
        }
    }

    public void markEmailAsVerified(String email) {
        String normalizedEmail = normalizeEmail(email);
        verifiedEmailCache.put(normalizedEmail, Boolean.TRUE);
        log.info("Email marked as verified in cache: {}", normalizedEmail);
    }

    public boolean isEmailVerified(String email) {
        String normalizedEmail = normalizeEmail(email);
        return Boolean.TRUE.equals(verifiedEmailCache.getIfPresent(normalizedEmail));
    }

    public void clearEmailVerification(String email) {
        String normalizedEmail = normalizeEmail(email);
        verifiedEmailCache.invalidate(normalizedEmail);
    }

    public void clearOtp(String email) {
        String normalizedEmail = normalizeEmail(email);
        otpCache.invalidate(normalizedEmail);
        attemptCache.invalidate(normalizedEmail);
    }

    private String normalizeEmail(String email) {
        return email != null ? email.trim().toLowerCase() : "";
    }
}
