package com.example.AuthProject.cache;

import com.example.AuthProject.dto.UserResponse;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class UserCacheService {
    private static final Logger log = LoggerFactory.getLogger(UserCacheService.class);

    private final Cache<String, UserResponse> cache;

    public UserCacheService(
            @Value("${app.cache.ttl-minutes:30}") long ttlMinutes,
            @Value("${app.cache.max-size:1000}") long maxSize
    ) {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(ttlMinutes))
                .maximumSize(maxSize)
                .build();
        log.info("Caffeine user cache initialized ttlMinutes={} maxSize={}", ttlMinutes, maxSize);
    }

    public void put(UserResponse user) {
        if (user == null || user.getUserEmail() == null || user.getUserEmail().isBlank()) {
            return;
        }
        cache.put(key(user.getUserEmail()), user);
        log.debug("Cached user details email={}", user.getUserEmail());
    }

    public Optional<UserResponse> getByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        UserResponse cached = cache.getIfPresent(key(email));
        if (cached != null) {
            log.debug("Cache hit email={}", email);
            return Optional.of(cached);
        }
        log.debug("Cache miss email={}", email);
        return Optional.empty();
    }

    public void evict(String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        cache.invalidate(key(email));
        log.debug("Evicted cached user email={}", email);
    }

    private String key(String email) {
        return email.trim().toLowerCase();
    }
}
