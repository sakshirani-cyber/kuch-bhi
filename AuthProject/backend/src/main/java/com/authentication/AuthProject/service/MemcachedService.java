package com.authentication.AuthProject.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.spy.memcached.MemcachedClient;
import org.springframework.stereotype.Service;

//for key/value operations.

@Slf4j
@RequiredArgsConstructor
@Service
public class MemcachedService {

    private final MemcachedClient memcachedClient;

    public void set(String key, int ttlSeconds, String value) {
        log.debug("Memcached SET key={} ttl={}s", key, ttlSeconds);
        memcachedClient.set(key, ttlSeconds, value);
    }

    public String get(String key) {
        log.debug("Memcached GET key={}", key);
        Object value = memcachedClient.get(key);
        return value != null ? value.toString() : null;
    }

    public void delete(String key) {
        log.debug("Deleting cache key: {}", key);
        memcachedClient.delete(key);
    }
}
