package com.clinicboard.user_service.infrastructure.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.clinicboard.user_service.infrastructure.config.security.filters.GatewayRequestFilter;
import com.clinicboard.user_service.infrastructure.config.security.filters.SecurityFilter;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * Configuração de segurança para o User Service.
 * 
 * Esta configuração implementa uma arquitetura de segurança em camadas:
 * 1. GatewayRequestFilter: Autentica requisições internas do Gateway
 * 2. SecurityFilter: Autentica requisições diretas com JWT
 * 
 * Seguindo os princípios de DDD, esta configuração permite que o contexto
 * de gestão de usuários receba requisições tanto diretas quanto do Gateway.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfigurations {

    private final SecurityFilter securityFilter;
    private final GatewayRequestFilter gatewayRequestFilter;

    public SecurityConfigurations(SecurityFilter securityFilter, GatewayRequestFilter gatewayRequestFilter) {
        this.securityFilter = securityFilter;
        this.gatewayRequestFilter = gatewayRequestFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // Endpoints públicos
                        .requestMatchers(HttpMethod.POST, "/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/metrics/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/health/**").permitAll()
                        // Todos os outros endpoints requerem autenticação
                        .anyRequest().authenticated())
                .addFilterBefore(gatewayRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}