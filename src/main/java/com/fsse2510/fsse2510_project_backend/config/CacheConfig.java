package com.fsse2510.fsse2510_project_backend.config;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Redis Cache configuration.
 * Default: 60 min TTL, no null caching, JSON serialization.
 * Per-cache overrides inherit the default JSON serializer and only customise
 * TTL.
 *
 * Uses FaultTolerantRedisSerializer to gracefully handle old cached data
 * (e.g. serialized without @class). On deserialization failure, returns null
 * so the cache treats it as miss and recomputes, then re-caches with the
 * current format.
 */
@Configuration
public class CacheConfig {

        @Bean
        public RedisCacheConfiguration cacheConfiguration() {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                mapper.activateDefaultTyping(
                                mapper.getPolymorphicTypeValidator(),
                                ObjectMapper.DefaultTyping.NON_FINAL,
                                JsonTypeInfo.As.WRAPPER_ARRAY);

                RedisSerializer<Object> jsonSerializer = new GenericJackson2JsonRedisSerializer(mapper);
                RedisSerializer<Object> faultTolerantSerializer = new FaultTolerantRedisSerializer<>(jsonSerializer);

                return RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(60))
                                .disableCachingNullValues()
                                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(faultTolerantSerializer));
        }

        @Bean
        public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(
                        RedisCacheConfiguration cacheConfiguration) {
                return (builder) -> builder
                                .withCacheConfiguration("product_v4",
                                                cacheConfiguration.entryTtl(Duration.ofHours(1)))
                                .withCacheConfiguration("product_recommendations_v4",
                                                cacheConfiguration.entryTtl(Duration.ofHours(1)))
                                .withCacheConfiguration("product_showcase_v1",
                                                cacheConfiguration.entryTtl(Duration.ofHours(1)))
                                .withCacheConfiguration("product_attributes_v4",
                                                cacheConfiguration.entryTtl(Duration.ofHours(12)))
                                .withCacheConfiguration("navigation_v1",
                                                cacheConfiguration.entryTtl(Duration.ofHours(24)))
                                .withCacheConfiguration("showcase_collections_v1",
                                                cacheConfiguration.entryTtl(Duration.ofHours(12)));
        }
}
