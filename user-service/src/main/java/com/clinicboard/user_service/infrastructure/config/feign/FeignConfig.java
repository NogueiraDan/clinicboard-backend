package com.clinicboard.user_service.infrastructure.config.feign;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import feign.Logger;

/**
 * Configuração do Feign Client para comunicação entre microsserviços
 */
@Configuration
public class FeignConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Configura o nível de log do Feign para debugging
     */
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }
}
