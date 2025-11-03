package com.clinicboard.gateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import com.clinicboard.gateway.model.UserInfo;
import com.clinicboard.gateway.application.service.JwtService;
import com.clinicboard.gateway.application.service.RedisService;

import java.time.Instant;

@Component
public class AuthenticationGatewayFilter implements GatewayFilter {

    private final JwtService jwtService;
    private final RedisService redisService;

    public AuthenticationGatewayFilter(
            JwtService jwtService,
            RedisService redisService) {
        this.jwtService = jwtService;
        this.redisService = redisService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);

        return validateTokenWithCache(token)
                .flatMap(userInfo -> {
                    // Adiciona headers com informações do usuário
                    ServerHttpRequest modifiedRequest = request.mutate()
                            .header("X-User-Id", userInfo.getUserId())
                            .header("X-User-Role", userInfo.getRole())
                            .header("X-User-Email", userInfo.getEmail())
                            .header("X-User-Name", userInfo.getName())
                            .build();

                    ServerWebExchange modifiedExchange = exchange.mutate()
                            .request(modifiedRequest)
                            .build();

                    return chain.filter(modifiedExchange);
                })
                .onErrorResume(throwable -> {
                    System.err.println("❌ Erro na autenticação: " + throwable.getMessage());
                    return unauthorized(exchange);
                });
    }

    private Mono<UserInfo> validateTokenWithCache(String token) {
        return redisService.getUserFromCache(token)
                .switchIfEmpty(
                        // Cache MISS - Valida JWT localmente e extrai claims
                        jwtService.validateTokenAndExtractClaims(token)
                                .flatMap(claims -> {
                                    // Criar UserInfo a partir dos claims do JWT
                                    UserInfo userInfo = new UserInfo(
                                            claims.getUserId(),
                                            claims.getEmail(),
                                            claims.getRole(),
                                            claims.getName(),
                                            claims.getContact(),
                                            Instant.now());

                                    // Cachear para próximas requisições
                                    return redisService.cacheUser(token, userInfo)
                                            .thenReturn(userInfo);
                                }));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}