package com.fsse2510.fsse2510_project_backend.config;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * Wraps a Redis serializer and returns null on deserialization failure.
 * This allows graceful migration when Redis contains old/incompatible cached data:
 * failed deserialization is treated as cache miss, triggering recomputation and
 * re-caching with the current format.
 */
public class FaultTolerantRedisSerializer<T> implements RedisSerializer<T> {

    private final RedisSerializer<T> delegate;

    public FaultTolerantRedisSerializer(RedisSerializer<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public byte[] serialize(T value) throws SerializationException {
        return delegate.serialize(value);
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return delegate.deserialize(bytes);
        } catch (SerializationException e) {
            return null;
        }
    }
}
