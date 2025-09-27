package com.clinicboard.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.clinicboard.gateway.model.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, UserInfo> userInfoRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {
        
        // Configurar ObjectMapper para suporte a Java Time (Instant)
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        // Configurar serializer JSON com ObjectMapper customizado
        Jackson2JsonRedisSerializer<UserInfo> valueSerializer = 
            new Jackson2JsonRedisSerializer<>(objectMapper, UserInfo.class);
        
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        
        // Configurar contexto completo de serialização incluindo hashKey e hashValue
        RedisSerializationContext<String, UserInfo> serializationContext = 
            RedisSerializationContext.<String, UserInfo>newSerializationContext()
                .key(stringSerializer)
                .value(valueSerializer)
                .hashKey(stringSerializer)
                .hashValue(valueSerializer)
                .build();
        
        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }
}