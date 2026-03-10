package com.fsse2510.fsse2510_project_backend.config;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

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
                return RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(60))
                                .disableCachingNullValues()
                                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(new org.springframework.data.redis.serializer.JdkSerializationRedisSerializer()));
        }

        @Bean
        public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(
                        RedisCacheConfiguration cacheConfiguration) {
                return (builder) -> builder
                                .withCacheConfiguration("product_v6",
                                                cacheConfiguration.entryTtl(Duration.ofHours(1)))
                                .withCacheConfiguration("product_recommendations_v6",
                                                cacheConfiguration.entryTtl(Duration.ofHours(1)))
                                .withCacheConfiguration("product_showcase_v3",
                                                cacheConfiguration.entryTtl(Duration.ofHours(1)))
                                .withCacheConfiguration("product_attributes_v6",
                                                cacheConfiguration.entryTtl(Duration.ofHours(12)))
                                .withCacheConfiguration("navigation_v3",
                                                cacheConfiguration.entryTtl(Duration.ofHours(24)))
                                .withCacheConfiguration("showcase_collections_v3",
                                                cacheConfiguration.entryTtl(Duration.ofHours(12)));
        }
}
