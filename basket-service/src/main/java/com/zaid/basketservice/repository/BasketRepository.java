package com.zaid.basketservice.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaid.basketservice.model.Basket;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class BasketRepository {

    private final StringRedisTemplate redisTemplate; // Spring'in built-in bean'i
    private final ObjectMapper redisObjectMapper;    // Bizim config'de yazdığımız bean

    private static final String KEY_PREFIX = "basket:";
    private static final Duration TTL = Duration.ofDays(7);

    public void saveBasket(Basket basket) {
        try {
            String key = KEY_PREFIX + basket.getUserId();
            // Nesneyi saf JSON String'e çeviriyoruz
            String jsonValue = redisObjectMapper.writeValueAsString(basket);
            redisTemplate.opsForValue().set(key, jsonValue, TTL);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Redis serileştirme hatası", e);
        }
    }

    public Basket getBasket(String userId) {
        String key = KEY_PREFIX + userId;
        String jsonValue = redisTemplate.opsForValue().get(key);

        if (jsonValue != null) {
            try {
                // Okuma yapıldığında TTL süresini yenile
                redisTemplate.expire(key, TTL);
                // JSON String'i tekrar Basket nesnesine çeviriyoruz
                return redisObjectMapper.readValue(jsonValue, Basket.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Redis deserileştirme hatası", e);
            }
        }
        return null;
    }

    public void deleteBasket(String userId) {
        String key = KEY_PREFIX + userId;
        redisTemplate.delete(key);
    }
}