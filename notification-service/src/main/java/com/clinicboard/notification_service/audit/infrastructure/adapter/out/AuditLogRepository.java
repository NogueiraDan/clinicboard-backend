package com.clinicboard.notification_service.audit.infrastructure.adapter.out;

import com.clinicboard.notification_service.audit.domain.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório JPA para persistência de logs de auditoria.
 * 
 * Spring Data JPA gera implementação automaticamente
 * baseada nos nomes dos métodos.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {

    /**
     * Busca todos os logs de auditoria de um agendamento.
     * Ordenados por data de ocorrência (mais antigo primeiro).
     */
    List<AuditLog> findByAggregateIdOrderByOccurredAtAsc(String aggregateId);

    /**
     * Busca log de auditoria por ID do evento.
     */
    Optional<AuditLog> findByEventId(String eventId);

    /**
     * Busca todos os logs de auditoria de um profissional.
     */
    List<AuditLog> findByProfessionalIdOrderByOccurredAtDesc(String professionalId);

    /**
     * Busca todos os logs ordenados por data (mais recente primeiro).
     */
    List<AuditLog> findAllByOrderByOccurredAtDesc();
}