package com.clinicboard.gateway.config;

import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Hooks;

import jakarta.annotation.PostConstruct;

/**
 * Configuração de Tracing Distribuído para o Gateway.
 * 
 * Garante que os traces sejam propagados corretamente através
 * dos microsserviços usando Spring Boot 3 + Micrometer Tracing.
 */
@Configuration
public class TracingConfiguration {

    /**
     * Habilita hooks de contexto para programação reativa.
     * Necessário para propagação correta de traces no WebFlux.
     */
    @PostConstruct
    public void enableTracingHooks() {
        Hooks.enableAutomaticContextPropagation();
    }
}