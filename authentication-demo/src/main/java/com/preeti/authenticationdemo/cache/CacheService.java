package com.preeti.authenticationdemo.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.preeti.authenticationdemo.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CacheService {

    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);
    private static final String KEY_PREFIX = "user:";

    private final Cache<String, User> userCache;

    public CacheService(Cache<String, User> userCache) {
        this.userCache = userCache;
    }

    public Optional<User> getUser(String username) {
        try {
            User cachedUser = userCache.getIfPresent(buildKey(username));
            if (cachedUser != null) {
                logger.debug("Cache HIT for username '{}'", username);
                return Optional.of(cachedUser);
            }
            logger.debug("Cache MISS for username '{}'", username);
        } catch (Exception exception) {
            logger.warn("Cache read failed for username '{}': falling back to database", username, exception);
        }
        return Optional.empty();
    }

    public void putUser(User user) {
        try {
            userCache.put(buildKey(user.getUsername()), user);
            logger.debug("Cached user '{}'", user.getUsername());
        } catch (Exception exception) {
            logger.warn("Cache write failed for username '{}'", user.getUsername(), exception);
        }
    }

    public void evictUser(String username) {
        try {
            userCache.invalidate(buildKey(username));
            logger.debug("Evicted user '{}' from cache", username);
        } catch (Exception exception) {
            logger.warn("Cache evict failed for username '{}'", username, exception);
        }
    }

    private String buildKey(String username) {
        return KEY_PREFIX + username;
    }

}
