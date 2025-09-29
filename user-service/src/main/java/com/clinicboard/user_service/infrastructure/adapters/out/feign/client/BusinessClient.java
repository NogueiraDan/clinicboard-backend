package com.clinicboard.user_service.infrastructure.adapters.out.feign.client;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.clinicboard.user_service.application.ports.out.BusinessPort;
import com.clinicboard.user_service.infrastructure.adapters.out.feign.BusinessFeignClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BusinessClient implements BusinessPort {

    private final BusinessFeignClient businessFeignClient;

    public BusinessClient(BusinessFeignClient businessFeignClient) {
        this.businessFeignClient = businessFeignClient;
    }

    @Override
    @Retry(name = "business-service", fallbackMethod = "findByUserIdFallback")
    @CircuitBreaker(name = "business-service", fallbackMethod = "findByUserIdFallback")
    public List<Optional<?>> findByUserId(String userId) {
        log.info("🔄 Buscando pacientes para usuário: {}", userId);
        return businessFeignClient.findByUserId(userId);
    }

    public List<Optional<?>> findByUserIdFallback(String userId, Throwable throwable) {
        log.warn("⚠️ Fallback executado para findByUserId com userId: {}. Reason: {}",
                userId, throwable.getMessage());
        log.debug("🔍 Stack trace completo:", throwable);
        return List.of(Optional.empty());
    }

}
