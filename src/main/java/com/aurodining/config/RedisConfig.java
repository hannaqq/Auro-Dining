package com.aurodining.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.aurodining.common.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Slf4j
@Configuration
public class RedisConfig implements CachingConfigurer {

    @Bean
    public CacheManager cacheManager(
            RedisConnectionFactory factory,
            @Value("${spring.cache.redis.time-to-live:30m}") Duration timeToLive) {
        // Use custom ObjectMapper to handle LocalDateTime and Long precision
        ObjectMapper mapper = new JacksonObjectMapper();

        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        // Initialize Jackson Serializer
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(mapper, Object.class);

        // Setup Cache Configuration
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(timeToLive)
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues();

        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .build();
    }

    /**
     * When Redis is down: cache get fails -> treat as cache miss -> method runs -> query DB.
     * Cache put/evict/clear failures are logged only; business logic (e.g. DB save) still succeeds.
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Cache get failed (Redis may be down), falling back to DB. cache={}, key={}, error={}",
                        cache.getName(), key, exception.getMessage());
                // Do not rethrow: Spring treats as cache miss and executes the cached method (DB query).
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("Cache put failed (Redis may be down). cache={}, key={}, error={}",
                        cache.getName(), key, exception.getMessage());
                // Do not rethrow: DB update already done; only caching fails.
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Cache evict failed (Redis may be down). cache={}, key={}, error={}",
                        cache.getName(), key, exception.getMessage());
                // Do not rethrow: evict is best-effort; stale cache is acceptable temporarily.
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn("Cache clear failed (Redis may be down). cache={}, error={}",
                        cache.getName(), exception.getMessage());
                // Do not rethrow: clear is best-effort.
            }
        };
    }
}

