package com.example.AuthProject.cache;

import com.example.AuthProject.dto.UserResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.utils.AddrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserCacheService {
    private static final Logger log = LoggerFactory.getLogger(UserCacheService.class);
    private static final String KEY_PREFIX = "user:email:";
    private static final int TTL_SECONDS = 1800;

    private final String memcachedServers;
    private MemcachedClient client;

    public UserCacheService(@Value("${app.memcached.servers:127.0.0.1:11211}") String memcachedServers) {
        this.memcachedServers = memcachedServers;
    }

    @PostConstruct
    public void init() {
        try {
            XMemcachedClientBuilder builder =
                    new XMemcachedClientBuilder(AddrUtil.getAddresses(memcachedServers));
            builder.setConnectionPoolSize(2);
            builder.setConnectTimeout(1000);
            builder.setOpTimeout(1000);
            client = builder.build();
            log.info("Memcached client connected to {}", memcachedServers);
        } catch (Exception ex) {
            client = null;
            log.warn("Memcached unavailable. Falling back to database only. reason={}", ex.getMessage());
        }
    }

    @PreDestroy
    public void shutdown() {
        if (client == null) {
            return;
        }
        try {
            client.shutdown();
        } catch (Exception ex) {
            log.warn("Error shutting down Memcached client: {}", ex.getMessage());
        }
    }

    public void put(UserResponse user) {
        if (client == null || user == null || user.getUserEmail() == null) {
            return;
        }
        try {
            client.set(key(user.getUserEmail()), TTL_SECONDS, user);
            log.debug("Cached user details email={}", user.getUserEmail());
        } catch (Exception ex) {
            log.warn("Failed to cache user email={}: {}", user.getUserEmail(), ex.getMessage());
        }
    }

    public Optional<UserResponse> getByEmail(String email) {
        if (client == null || email == null || email.isBlank()) {
            return Optional.empty();
        }
        try {
            UserResponse cached = client.get(key(email));
            if (cached != null) {
                log.debug("Cache hit email={}", email);
                return Optional.of(cached);
            }
            log.debug("Cache miss email={}", email);
        } catch (Exception ex) {
            log.warn("Failed to read cache email={}: {}", email, ex.getMessage());
        }
        return Optional.empty();
    }

    public void evict(String email) {
        if (client == null || email == null || email.isBlank()) {
            return;
        }
        try {
            client.delete(key(email));
            log.debug("Evicted cached user email={}", email);
        } catch (Exception ex) {
            log.warn("Failed to evict cache email={}: {}", email, ex.getMessage());
        }
    }

    private String key(String email) {
        return KEY_PREFIX + email.trim().toLowerCase();
    }
}
