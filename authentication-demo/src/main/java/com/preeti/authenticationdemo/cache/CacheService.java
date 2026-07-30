package com.preeti.authenticationdemo.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.preeti.authenticationdemo.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CacheService {

    private static final String USER_KEY_PREFIX = "user:";
    private static final String OTP_KEY_PREFIX = "otp:";
    private static final String ATTEMPTS_KEY_PREFIX = "otp:attempts:";

    private final Cache<String, User> userCache;
    private final Cache<String, String> otpCache;
    private final Cache<String, Integer> otpAttemptsCache;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    public CacheService(Cache<String, User> userCache,
                        Cache<String, String> otpCache,
                        Cache<String, Integer> otpAttemptsCache) {
        this.userCache = userCache;
        this.otpCache = otpCache;
        this.otpAttemptsCache = otpAttemptsCache;
    }

    // ================= User Profile Cache (10 min TTL) =================

    public Optional<User> getUser(String username) {
        String key = buildUserKey(username);
        if (redisTemplate != null) {
            try {
                Object cachedObj = redisTemplate.opsForValue().get(key);
                if (cachedObj instanceof User user) {
                    log.debug("Redis Cache HIT for username '{}'", username);
                    return Optional.of(user);
                }
            } catch (Exception exception) {
                log.warn("Redis read failed for username '{}', trying Caffeine fallback: {}", username, exception.getMessage());
            }
        }

        try {
            User cachedUser = userCache.getIfPresent(key);
            if (cachedUser != null) {
                log.debug("Caffeine Cache HIT for username '{}'", username);
                return Optional.of(cachedUser);
            }
        } catch (Exception exception) {
            log.warn("Caffeine read failed for username '{}'", username, exception);
        }

        return Optional.empty();
    }

    public void putUser(User user) {
        String key = buildUserKey(user.getUsername());
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(key, user, 10, TimeUnit.MINUTES);
                log.debug("Stored user profile for '{}' in Redis (TTL 10m)", user.getUsername());
            } catch (Exception exception) {
                log.warn("Redis write failed for username '{}': {}", user.getUsername(), exception.getMessage());
            }
        }

        try {
            userCache.put(key, user);
            log.debug("Stored user profile for '{}' in Caffeine cache", user.getUsername());
        } catch (Exception exception) {
            log.warn("Caffeine write failed for username '{}'", user.getUsername(), exception);
        }
    }

    public void evictUser(String username) {
        String key = buildUserKey(username);
        if (redisTemplate != null) {
            try {
                redisTemplate.delete(key);
                log.debug("Evicted user '{}' from Redis cache", username);
            } catch (Exception exception) {
                log.warn("Redis evict failed for username '{}': {}", username, exception.getMessage());
            }
        }

        try {
            userCache.invalidate(key);
            log.debug("Evicted user '{}' from Caffeine cache", username);
        } catch (Exception exception) {
            log.warn("Caffeine evict failed for username '{}'", username, exception);
        }
    }

    // ================= OTP & Attempts Cache (5 min TTL) =================

    public void saveOtp(String email, String otp) {
        String normalizedEmail = email.trim().toLowerCase();
        String otpKey = buildOtpKey(normalizedEmail);
        String attemptsKey = buildAttemptsKey(normalizedEmail);

        if (stringRedisTemplate != null) {
            try {
                stringRedisTemplate.opsForValue().set(otpKey, otp, 5, TimeUnit.MINUTES);
                stringRedisTemplate.opsForValue().set(attemptsKey, "0", 5, TimeUnit.MINUTES);
                log.debug("Stored OTP for email '{}' in Redis (TTL 5m)", normalizedEmail);
            } catch (Exception exception) {
                log.warn("Redis saveOtp failed for email '{}': {}", normalizedEmail, exception.getMessage());
            }
        }

        try {
            otpCache.put(otpKey, otp);
            otpAttemptsCache.put(attemptsKey, 0);
            log.debug("Stored OTP for email '{}' in Caffeine cache", normalizedEmail);
        } catch (Exception exception) {
            log.warn("Caffeine saveOtp failed for email '{}'", normalizedEmail, exception);
        }
    }

    public Optional<String> getOtp(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        String otpKey = buildOtpKey(normalizedEmail);

        if (stringRedisTemplate != null) {
            try {
                String redisOtp = stringRedisTemplate.opsForValue().get(otpKey);
                if (redisOtp != null) {
                    return Optional.of(redisOtp);
                }
            } catch (Exception exception) {
                log.warn("Redis getOtp failed for email '{}': {}", normalizedEmail, exception.getMessage());
            }
        }

        try {
            String caffeineOtp = otpCache.getIfPresent(otpKey);
            return Optional.ofNullable(caffeineOtp);
        } catch (Exception exception) {
            log.warn("Caffeine getOtp failed for email '{}'", normalizedEmail, exception);
            return Optional.empty();
        }
    }

    public void invalidateOtp(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        String otpKey = buildOtpKey(normalizedEmail);
        String attemptsKey = buildAttemptsKey(normalizedEmail);

        if (stringRedisTemplate != null) {
            try {
                stringRedisTemplate.delete(otpKey);
                stringRedisTemplate.delete(attemptsKey);
                log.debug("Invalidated Redis OTP for email '{}'", normalizedEmail);
            } catch (Exception exception) {
                log.warn("Redis invalidateOtp failed for email '{}': {}", normalizedEmail, exception.getMessage());
            }
        }

        try {
            otpCache.invalidate(otpKey);
            otpAttemptsCache.invalidate(attemptsKey);
            log.debug("Invalidated Caffeine OTP for email '{}'", normalizedEmail);
        } catch (Exception exception) {
            log.warn("Caffeine invalidateOtp failed for email '{}'", normalizedEmail, exception);
        }
    }

    public int incrementAttempts(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        String attemptsKey = buildAttemptsKey(normalizedEmail);

        if (stringRedisTemplate != null) {
            try {
                Long incremented = stringRedisTemplate.opsForValue().increment(attemptsKey);
                stringRedisTemplate.expire(attemptsKey, 5, TimeUnit.MINUTES);
                if (incremented != null) {
                    otpAttemptsCache.put(attemptsKey, incremented.intValue());
                    return incremented.intValue();
                }
            } catch (Exception exception) {
                log.warn("Redis incrementAttempts failed for email '{}': {}", normalizedEmail, exception.getMessage());
            }
        }

        try {
            Integer current = otpAttemptsCache.getIfPresent(attemptsKey);
            int updated = (current != null ? current : 0) + 1;
            otpAttemptsCache.put(attemptsKey, updated);
            return updated;
        } catch (Exception exception) {
            log.warn("Caffeine incrementAttempts failed for email '{}'", normalizedEmail, exception);
            return 1;
        }
    }

    public int getAttempts(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        String attemptsKey = buildAttemptsKey(normalizedEmail);

        if (stringRedisTemplate != null) {
            try {
                String val = stringRedisTemplate.opsForValue().get(attemptsKey);
                if (val != null) {
                    return Integer.parseInt(val);
                }
            } catch (Exception exception) {
                log.warn("Redis getAttempts failed for email '{}': {}", normalizedEmail, exception.getMessage());
            }
        }

        try {
            Integer attempts = otpAttemptsCache.getIfPresent(attemptsKey);
            return attempts != null ? attempts : 0;
        } catch (Exception exception) {
            log.warn("Caffeine getAttempts failed for email '{}'", normalizedEmail, exception);
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
