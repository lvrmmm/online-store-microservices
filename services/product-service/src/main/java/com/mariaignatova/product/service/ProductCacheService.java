package com.mariaignatova.product.service;

import com.mariaignatova.product.dto.ProductDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductCacheService {

    private static final Logger log = LoggerFactory.getLogger(ProductCacheService.class);
    private static final String PREFIX = "product:";
    private static final Duration TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public ProductCacheService(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    public Optional<ProductDto> get(UUID id) {
        String json = redis.opsForValue().get(PREFIX + id);
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, ProductDto.class));
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize cached product {}", id, e);
            return Optional.empty();
        }
    }

    public void put(ProductDto product) {
        try {
            redis.opsForValue().set(PREFIX + product.id(),
                    objectMapper.writeValueAsString(product), TTL);
        } catch (JsonProcessingException e) {
            log.warn("Failed to cache product {}", product.id(), e);
        }
    }

    public void evict(UUID id) {
        redis.delete(PREFIX + id);
    }
}