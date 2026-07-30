package com.example.AuthProject.cache;

import com.example.AuthProject.dto.UserResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
public class UserCacheService {
    private static final String USER_KEY_PREFIX = "user:";
    private static final String USER_EMAIL_KEY_PREFIX = "user:email:";

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final Duration ttl;

    public UserCacheService(
            StringRedisTemplate redis,
            ObjectMapper objectMapper,
            @Value("${app.cache.user-ttl-seconds:600}") long userTtlSeconds
    ) {
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.ttl = Duration.ofSeconds(userTtlSeconds);
        log.info("Redis user profile cache initialized ttlSeconds={}", userTtlSeconds);
    }

    public void putUserById(UserResponse user) {
        if (user == null || user.getUserId() == null) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(user);
            String idKey = idKey(user.getUserId());
            redis.opsForValue().set(idKey, json, ttl);
            if (user.getUserEmail() != null && !user.getUserEmail().isBlank()) {
                redis.opsForValue().set(emailKey(user.getUserEmail()), String.valueOf(user.getUserId()), ttl);
            }
            log.debug("Cached user profile userId={}", user.getUserId());
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize user for Redis cache userId={}: {}", user.getUserId(), e.getMessage());
        } catch (Exception e) {
            log.warn("Redis putUserById failed userId={}: {}", user.getUserId(), e.getMessage());
        }
    }

    public Optional<UserResponse> getUserById(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }
        try {
            String json = redis.opsForValue().get(idKey(userId));
            if (json == null || json.isBlank()) {
                log.debug("Cache miss userId={}", userId);
                return Optional.empty();
            }
            UserResponse cached = objectMapper.readValue(json, UserResponse.class);
            log.debug("Cache hit userId={}", userId);
            return Optional.of(cached);
        } catch (Exception e) {
            log.warn("Redis getUserById failed userId={}: {}", userId, e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<UserResponse> getByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        try {
            String userIdStr = redis.opsForValue().get(emailKey(email));
            if (userIdStr == null || userIdStr.isBlank()) {
                log.debug("Cache miss by email={}", email);
                return Optional.empty();
            }
            Long userId = Long.valueOf(userIdStr);
            return getUserById(userId);
        } catch (Exception e) {
            log.warn("Redis getByEmail failed email={}: {}", email, e.getMessage());
            return Optional.empty();
        }
    }

    public void evictUserById(Long userId) {
        if (userId == null) {
            return;
        }
        try {
            String idKey = idKey(userId);
            String json = redis.opsForValue().get(idKey);
            if (json != null && !json.isBlank()) {
                try {
                    UserResponse cached = objectMapper.readValue(json, UserResponse.class);
                    if (cached.getUserEmail() != null && !cached.getUserEmail().isBlank()) {
                        redis.delete(emailKey(cached.getUserEmail()));
                    }
                } catch (JsonProcessingException e) {
                    log.warn("Failed to parse cached user during eviction userId={}: {}", userId, e.getMessage());
                }
            }
            redis.delete(idKey);
            log.debug("Evicted cached user userId={}", userId);
        } catch (Exception e) {
            log.warn("Redis evictUserById failed userId={}: {}", userId, e.getMessage());
        }
    }

    private String idKey(Long userId) {
        return USER_KEY_PREFIX + userId;
    }

    private String emailKey(String email) {
        return USER_EMAIL_KEY_PREFIX + email.trim().toLowerCase();
    }
}
