package com.authentication.AuthProject.service;

import com.authentication.AuthProject.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

//Caches payloads in Memcached as JSON.

@Slf4j
@RequiredArgsConstructor
@Service
public class UserProfileCacheService {

    private static final int USER_PROFILE_TTL_SECONDS = 600;

    private final MemcachedService memcachedService;
    private final ObjectMapper objectMapper;

    public Optional<UserResponse> getCachedUser(Long id) {
        String key = cacheKey(id);
        try {
            String json = memcachedService.get(key);
            if (json == null) {
                log.debug("Cache MISS for user ID: {}", id);
                return Optional.empty();
            }
            UserResponse response = objectMapper.readValue(json, UserResponse.class);
            log.debug("Cache HIT for user ID: {}", id);
            return Optional.of(response);
        } catch (Exception ex) {
            log.warn("Failed to read user profile cache for ID {}: {}", id, ex.getMessage());
            try {
                memcachedService.delete(key);
            } catch (Exception deleteEx) {
                log.warn("Failed to delete corrupt cache entry for ID {}: {}", id, deleteEx.getMessage());
            }
            return Optional.empty();
        }
    }

    public void cacheUser(UserResponse response) {
        if (response == null || response.getId() == null) {
            return;
        }
        String key = cacheKey(response.getId());
        try {
            String json = objectMapper.writeValueAsString(response);
            memcachedService.set(key, USER_PROFILE_TTL_SECONDS, json);
            log.debug("Cache populated for user ID: {}", response.getId());
        } catch (JacksonException ex) {
            log.warn("Failed to serialize user profile for cache ID {}: {}", response.getId(), ex.getMessage());
        } catch (Exception ex) {
            log.warn("Failed to cache user profile for ID {}: {}", response.getId(), ex.getMessage());
        }
    }

    public void evictUser(Long id) {
        if (id == null) {
            return;
        }
        try {
            memcachedService.delete(cacheKey(id));
            log.debug("Cache evicted for user ID: {}", id);
        } catch (Exception ex) {
            log.warn("Failed to evict user profile cache for ID {}: {}", id, ex.getMessage());
        }
    }

    private String cacheKey(Long id) {
        return "user:" + id;
    }
}
