package com.authentication.AuthProject.service;

import com.authentication.AuthProject.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserProfileCacheService {

    private static final int USER_PROFILE_TTL_SECONDS = 600;
    private static final String CACHE_KEY_PREFIX = "user:email:";

    private final MemcachedService memcachedService;
    private final ObjectMapper objectMapper;

    public Optional<UserResponse> getCachedUser(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail.isEmpty()) {
            return Optional.empty();
        }
        String key = cacheKey(normalizedEmail);
        try {
            String json = memcachedService.get(key);
            if (json == null) {
                log.debug("Cache MISS for user email: {}", normalizedEmail);
                return Optional.empty();
            }
            UserResponse response = objectMapper.readValue(json, UserResponse.class);
            log.debug("Cache HIT for user email: {}", normalizedEmail);
            return Optional.of(response);
        } catch (Exception ex) {
            log.warn("Failed to read user profile cache for email {}: {}", normalizedEmail, ex.getMessage());
            try {
                memcachedService.delete(key);
            } catch (Exception deleteEx) {
                log.warn("Failed to delete corrupt cache entry for email {}: {}", normalizedEmail, deleteEx.getMessage());
            }
            return Optional.empty();
        }
    }

    public void cacheUser(UserResponse response) {
        if (response == null || response.getEmail() == null || response.getEmail().isBlank()) {
            return;
        }
        String normalizedEmail = normalizeEmail(response.getEmail());
        String key = cacheKey(normalizedEmail);
        try {
            String json = objectMapper.writeValueAsString(response);
            memcachedService.set(key, USER_PROFILE_TTL_SECONDS, json);
            log.debug("Cache populated for user email: {}", normalizedEmail);
        } catch (JacksonException ex) {
            log.warn("Failed to serialize user profile for cache email {}: {}", normalizedEmail, ex.getMessage());
        } catch (Exception ex) {
            log.warn("Failed to cache user profile for email {}: {}", normalizedEmail, ex.getMessage());
        }
    }

    public void evictUser(String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        String normalizedEmail = normalizeEmail(email);
        try {
            memcachedService.delete(cacheKey(normalizedEmail));
            log.debug("Cache evicted for user email: {}", normalizedEmail);
        } catch (Exception ex) {
            log.warn("Failed to evict user profile cache for email {}: {}", normalizedEmail, ex.getMessage());
        }
    }

    private String cacheKey(String normalizedEmail) {
        return CACHE_KEY_PREFIX + normalizedEmail;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
