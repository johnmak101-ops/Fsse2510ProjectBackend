package com.fsse2510.fsse2510_project_backend.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

/**
 * Redis Cache configuration.
 * Default: 60 min TTL, no null caching, Advanced JSON serialization.
 */
@Configuration
public class CacheConfig {

        @Bean
        public RedisCacheConfiguration cacheConfiguration() {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                mapper.activateDefaultTyping(
                                LaissezFaireSubTypeValidator.instance,
                                ObjectMapper.DefaultTyping.NON_FINAL,
                                JsonTypeInfo.As.PROPERTY);

                return RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(60))
                                .disableCachingNullValues()
                                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(new GenericJackson2JsonRedisSerializer(mapper)));
        }

        @Bean
        public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(
                        RedisCacheConfiguration cacheConfiguration) {
                return (builder) -> builder
                                .withCacheConfiguration("product_v8",
                                                cacheConfiguration.entryTtl(Duration.ofHours(1)))
                                .withCacheConfiguration("product_recommendations_v8",
                                                cacheConfiguration.entryTtl(Duration.ofHours(1)))
                                .withCacheConfiguration("product_showcase_v5",
                                                cacheConfiguration.entryTtl(Duration.ofHours(1)))
                                .withCacheConfiguration("product_attributes_v8",
                                                cacheConfiguration.entryTtl(Duration.ofHours(12)))
                                .withCacheConfiguration("navigation_v5",
                                                cacheConfiguration.entryTtl(Duration.ofHours(24)))
                                .withCacheConfiguration("showcase_collections_v5",
                                                cacheConfiguration.entryTtl(Duration.ofHours(12)))
                                .withCacheConfiguration("users",
                                                cacheConfiguration.entryTtl(Duration.ofMinutes(30)))
                                .withCacheConfiguration("transactions",
                                                cacheConfiguration.entryTtl(Duration.ofMinutes(30)));
        }
}
