package com.clinicboard.business_service.infrastructure.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração de beans para garantir que componentes sejam detectados
 * Implementa o padrão de configuração da Arquitetura Hexagonal
 */
@Configuration
@ComponentScan(basePackages = {
    "com.clinicboard.business_service.application.usecase",
    "com.clinicboard.business_service.infrastructure.adapter"
})
public class ApplicationConfig {
    // Configuration class para scan de componentes
}
