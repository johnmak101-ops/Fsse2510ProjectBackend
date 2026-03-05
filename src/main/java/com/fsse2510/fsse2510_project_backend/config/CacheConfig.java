package com.fsse2510.fsse2510_project_backend.config;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

/**
 * Redis Cache configuration(see baeldung.com/spring-boot-redis-cache)
 * Default: 60mins, no null cache, and view Redis JSON for debug use
 * Customizer: product_attributes -> Size, Color,...
 */

@Configuration
public class CacheConfig {

        @Bean
        public RedisCacheConfiguration cacheConfiguration() {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                mapper.activateDefaultTyping(mapper.getPolymorphicTypeValidator(),
                                com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.NON_FINAL,
                                com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY);

                return RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(60)) // Default TTL 1 hour
                                .disableCachingNullValues()
                                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(new GenericJackson2JsonRedisSerializer(mapper)));
        }

        @Bean
        public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
                return (builder) -> builder
                                .withCacheConfiguration("product",
                                                RedisCacheConfiguration.defaultCacheConfig()
                                                                .entryTtl(Duration.ofHours(1)))
                                .withCacheConfiguration("product_recommendations",
                                                RedisCacheConfiguration.defaultCacheConfig()
                                                                .entryTtl(Duration.ofHours(1)))
                                .withCacheConfiguration("product_showcase",
                                                RedisCacheConfiguration.defaultCacheConfig()
                                                                .entryTtl(Duration.ofHours(1)))
                                .withCacheConfiguration("product_attributes",
                                                RedisCacheConfiguration.defaultCacheConfig()
                                                                .entryTtl(Duration.ofHours(12)));
        }
}
