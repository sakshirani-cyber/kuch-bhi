package com.preeti.authenticationdemo.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.preeti.authenticationdemo.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class CacheService {

    private static final String USER_KEY_PREFIX = "user:";
    private static final String OTP_KEY_PREFIX = "otp:";
    private static final String ATTEMPTS_KEY_PREFIX = "otp:attempts:";

    private final Cache<String, User> userCache;
    private final Cache<String, String> otpCache;
    private final Cache<String, Integer> otpAttemptsCache;

    public CacheService(Cache<String, User> userCache,
                        Cache<String, String> otpCache,
                        Cache<String, Integer> otpAttemptsCache) {
        this.userCache = userCache;
        this.otpCache = otpCache;
        this.otpAttemptsCache = otpAttemptsCache;
    }

    // ================= User Profile Cache (10 min TTL) =================

    public Optional<User> getUser(String username) {
        try {
            User cachedUser = userCache.getIfPresent(buildUserKey(username));
            if (cachedUser != null) {
                log.debug("Cache HIT for username '{}'", username);
                return Optional.of(cachedUser);
            }
            log.debug("Cache MISS for username '{}'", username);
        } catch (Exception exception) {
            log.warn("Cache read failed for username '{}': falling back to database", username, exception);
        }
        return Optional.empty();
    }

    public void putUser(User user) {
        try {
            userCache.put(buildUserKey(user.getUsername()), user);
            log.debug("Cached user profile for '{}'", user.getUsername());
        } catch (Exception exception) {
            log.warn("Cache write failed for username '{}'", user.getUsername(), exception);
        }
    }

    public void evictUser(String username) {
        try {
            userCache.invalidate(buildUserKey(username));
            log.debug("Evicted user '{}' from profile cache", username);
        } catch (Exception exception) {
            log.warn("Cache evict failed for username '{}'", username, exception);
        }
    }

    // ================= OTP & Attempts Cache (5 min TTL) =================

    public void saveOtp(String email, String otp) {
        String normalizedEmail = email.trim().toLowerCase();
        try {
            otpCache.put(buildOtpKey(normalizedEmail), otp);
            otpAttemptsCache.put(buildAttemptsKey(normalizedEmail), 0);
            log.debug("Stored OTP for email '{}' in Caffeine cache", normalizedEmail);
        } catch (Exception exception) {
            log.warn("Failed to store OTP in cache for email '{}'", normalizedEmail, exception);
        }
    }

    public Optional<String> getOtp(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        try {
            String otp = otpCache.getIfPresent(buildOtpKey(normalizedEmail));
            return Optional.ofNullable(otp);
        } catch (Exception exception) {
            log.warn("Failed to read OTP from cache for email '{}'", normalizedEmail, exception);
            return Optional.empty();
        }
    }

    public void invalidateOtp(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        try {
            otpCache.invalidate(buildOtpKey(normalizedEmail));
            otpAttemptsCache.invalidate(buildAttemptsKey(normalizedEmail));
            log.debug("Invalidated OTP & attempt counter for email '{}'", normalizedEmail);
        } catch (Exception exception) {
            log.warn("Failed to invalidate OTP in cache for email '{}'", normalizedEmail, exception);
        }
    }

    public int incrementAttempts(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        try {
            String attemptsKey = buildAttemptsKey(normalizedEmail);
            Integer current = otpAttemptsCache.getIfPresent(attemptsKey);
            int updated = (current != null ? current : 0) + 1;
            otpAttemptsCache.put(attemptsKey, updated);
            return updated;
        } catch (Exception exception) {
            log.warn("Failed to increment OTP attempts for email '{}'", normalizedEmail, exception);
            return 1;
        }
    }

    public int getAttempts(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        try {
            Integer attempts = otpAttemptsCache.getIfPresent(buildAttemptsKey(normalizedEmail));
            return attempts != null ? attempts : 0;
        } catch (Exception exception) {
            log.warn("Failed to read OTP attempts for email '{}'", normalizedEmail, exception);
            return 0;
        }
    }

    // ================= Key Helper Methods =================

    private String buildUserKey(String username) {
        return USER_KEY_PREFIX + username.trim().toLowerCase();
    }

    private String buildOtpKey(String email) {
        return OTP_KEY_PREFIX + email.trim().toLowerCase();
    }

    private String buildAttemptsKey(String email) {
        return ATTEMPTS_KEY_PREFIX + email.trim().toLowerCase();
    }

}
