package com.example.SpringJWT.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

// @Configuration — tells Spring this class defines beans
@Configuration
public class CacheConfig {

    // RedisCacheManager — the CacheManager that @Cacheable / @CacheEvict uses
    // Spring Boot 4.x does NOT auto-create this, so we must define it manually.
    // It uses the RedisConnectionFactory bean (auto-created from application.yml).
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        return RedisCacheManager.create(redisConnectionFactory);
    }
}
