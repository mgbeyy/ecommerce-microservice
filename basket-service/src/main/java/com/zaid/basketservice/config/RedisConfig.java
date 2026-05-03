package com.zaid.basketservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

    // Artık RedisTemplate bean'ine gerek yok, StringRedisTemplate zaten Spring tarafından otomatik oluşturuluyor.
    // Biz sadece JSON dönüşümleri için tarih formatını düzelttiğimiz ObjectMapper'ı Bean yapıyoruz.
    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
}