package com.preeti.authenticationdemo.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.preeti.authenticationdemo.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class CacheService {

    private static final String KEY_PREFIX = "user:";

    private final Cache<String, User> userCache;

    public CacheService(Cache<String, User> userCache) {
        this.userCache = userCache;
    }

    public Optional<User> getUser(String username) {
        try {
            User cachedUser = userCache.getIfPresent(buildKey(username));
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
            userCache.put(buildKey(user.getUsername()), user);
            log.debug("Cached user '{}'", user.getUsername());
        } catch (Exception exception) {
            log.warn("Cache write failed for username '{}'", user.getUsername(), exception);
        }
    }

    public void evictUser(String username) {
        try {
            userCache.invalidate(buildKey(username));
            log.debug("Evicted user '{}' from cache", username);
        } catch (Exception exception) {
            log.warn("Cache evict failed for username '{}'", username, exception);
        }
    }

    private String buildKey(String username) {
        return KEY_PREFIX + username;
    }

}
