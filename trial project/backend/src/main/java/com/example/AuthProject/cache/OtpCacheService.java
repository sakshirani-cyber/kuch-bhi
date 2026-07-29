package com.example.AuthProject.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
public class OtpCacheService {
    private static final String OTP_PREFIX = "otp:";
    private static final String ATTEMPTS_PREFIX = "otp:attempts:";

    private final Cache<String, String> otpCache;
    private final Cache<String, Integer> attemptsCache;

    public OtpCacheService(
            @Value("${app.otp.ttl-seconds:300}") long otpTtlSeconds,
            @Value("${app.cache.max-size:1000}") long maxSize
    ) {
        Duration ttl = Duration.ofSeconds(otpTtlSeconds);
        this.otpCache = Caffeine.newBuilder()
                .expireAfterWrite(ttl)
                .maximumSize(maxSize)
                .build();
        this.attemptsCache = Caffeine.newBuilder()
                .expireAfterWrite(ttl)
                .maximumSize(maxSize)
                .build();
        log.info("Caffeine OTP caches initialized ttlSeconds={} maxSize={}", otpTtlSeconds, maxSize);
    }

    public void putOtp(String email, String otp) {
        String normalized = normalize(email);
        otpCache.put(OTP_PREFIX + normalized, otp);
        attemptsCache.invalidate(ATTEMPTS_PREFIX + normalized);
        log.debug("Stored OTP for email={}", normalized);
    }

    public Optional<String> getOtp(String email) {
        String value = otpCache.getIfPresent(OTP_PREFIX + normalize(email));
        return Optional.ofNullable(value);
    }

    public void deleteOtp(String email) {
        String normalized = normalize(email);
        otpCache.invalidate(OTP_PREFIX + normalized);
        log.debug("Deleted OTP for email={}", normalized);
    }

    public int incrementAttempts(String email) {
        String key = ATTEMPTS_PREFIX + normalize(email);
        Integer current = attemptsCache.getIfPresent(key);
        int next = (current == null ? 0 : current) + 1;
        attemptsCache.put(key, next);
        return next;
    }

    public int getAttempts(String email) {
        Integer current = attemptsCache.getIfPresent(ATTEMPTS_PREFIX + normalize(email));
        return current == null ? 0 : current;
    }

    public void deleteAttempts(String email) {
        attemptsCache.invalidate(ATTEMPTS_PREFIX + normalize(email));
    }

    public void clearOtpState(String email) {
        deleteOtp(email);
        deleteAttempts(email);
    }

    private String normalize(String email) {
        return email.trim().toLowerCase();
    }
}
