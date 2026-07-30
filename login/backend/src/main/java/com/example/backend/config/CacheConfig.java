package com.example.backend.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    /*
    // Old Caffeine CacheManager implementation:


	
    // @Bean
    // public CacheManager cacheManager() {
    //     CaffeineCacheManager cacheManager = new CaffeineCacheManager("users");
    //     cacheManager.setCaffeine(Caffeine.newBuilder()
    //             .initialCapacity(100)
    //             .maximumSize(500)
    //             .expireAfterWrite(10, TimeUnit.MINUTES)
    //             .recordStats());
    //     return cacheManager;
    // }
    */





    /*
    // Old RedisCacheConfiguration without JavaTimeModule (caused SerializationException on LocalDate):




    // @Bean
    // public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    //     RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
    //             .entryTtl(Duration.ofMinutes(10))
    //             .disableCachingNullValues()
    //             .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
    //     return RedisCacheManager.builder(connectionFactory).cacheDefaults(cacheConfig).build();
    // }
    */

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        return RedisCacheManager.builder(connectionFactory).cacheDefaults(cacheConfig).build();
    }
}
