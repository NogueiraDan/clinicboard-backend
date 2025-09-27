package com.clinicboard.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

        private final AuthenticationGatewayFilter authenticationGatewayFilter;

        public GatewayConfig(AuthenticationGatewayFilter authenticationGatewayFilter) {
                this.authenticationGatewayFilter = authenticationGatewayFilter;
        }

        @Bean
        public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
                return builder.routes()
                                // =================================================================
                                // 🚨 PUBLIC ROUTES (SEM AUTENTICAÇÃO) - ORDEM ESPECÍFICA PRIMEIRO
                                // =================================================================

                                // Gateway Health Check
                                .route("gateway-health", r -> r.path("/actuator/**")
                                                .uri("lb://gateway"))

                                // User Service - Health checks
                                .route("user-service-health", r -> r.path("/user-service/actuator/**")
                                                .filters(f -> f.rewritePath("/user-service/(?<segment>.*)",
                                                                "/${segment}"))
                                                .uri("lb://user-service"))

                                // User Service - Auth endpoints (login/register) - ROTA CORRIGIDA
                                .route("user-service-auth", r -> r.path("/user-service/auth/**")
                                                .filters(f -> f.rewritePath("/user-service/(?<segment>.*)",
                                                                "/${segment}"))
                                                .uri("lb://user-service"))

                                // =================================================================
                                // 🔒 PROTECTED ROUTES (COM AUTENTICAÇÃO)
                                // =================================================================

                                // User Service - Admin only routes
                                .route("user-service-users-admin", r -> r.path("/user-service/users")
                                                .and().method("GET", "DELETE")
                                                .filters(f -> f
                                                                .filter(authenticationGatewayFilter)
                                                                .rewritePath("/user-service/(?<segment>.*)",
                                                                                "/${segment}"))
                                                .uri("lb://user-service"))

                                // User Service - User management routes
                                .route("user-service-users-general", r -> r.path("/user-service/users/**")
                                                .filters(f -> f
                                                                .filter(authenticationGatewayFilter)
                                                                .rewritePath("/user-service/(?<segment>.*)",
                                                                                "/${segment}"))
                                                .uri("lb://user-service"))

                                // Scheduling Service - All routes protected
                                .route("scheduling-service-protected", r -> r.path("/scheduling-service/**")
                                                .filters(f -> f
                                                                .filter(authenticationGatewayFilter)
                                                                .rewritePath("/scheduling-service/(?<segment>.*)",
                                                                                "/${segment}"))
                                                .uri("lb://scheduling-service"))

                                .build();
        }
}