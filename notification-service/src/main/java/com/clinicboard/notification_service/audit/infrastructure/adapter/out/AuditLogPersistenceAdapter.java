package com.clinicboard.notification_service.audit.infrastructure.adapter.out;

import com.clinicboard.notification_service.audit.application.port.out.AuditLogPersistencePort;
import com.clinicboard.notification_service.audit.domain.model.AuditLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de saída que implementa a porta de persistência.
 * 
 * Responsabilidades:
 * - Implementar interface de persistência definida pela aplicação
 * - Delegar operações para repositório JPA
 * - Isolar camada de aplicação de detalhes do Spring Data
 * 
 * Segue o padrão Hexagonal Architecture onde adaptadores
 * de saída implementam portas de saída da camada de aplicação.
 */
@Component
@RequiredArgsConstructor
public class AuditLogPersistenceAdapter implements AuditLogPersistencePort {

    private final AuditLogRepository auditLogRepository;

    @Override
    public AuditLog save(AuditLog auditLog) {
        return auditLogRepository.save(auditLog);
    }

    @Override
    public List<AuditLog> findByAggregateId(String aggregateId) {
        return auditLogRepository.findByAggregateIdOrderByOccurredAtAsc(aggregateId);
    }

    @Override
    public Optional<AuditLog> findByEventId(String eventId) {
        return auditLogRepository.findByEventId(eventId);
    }

    @Override
    public List<AuditLog> findByProfessionalId(String professionalId) {
        return auditLogRepository.findByProfessionalIdOrderByOccurredAtDesc(professionalId);
    }

    @Override
    public List<AuditLog> findAll() {
        return auditLogRepository.findAllByOrderByOccurredAtDesc();
    }
}