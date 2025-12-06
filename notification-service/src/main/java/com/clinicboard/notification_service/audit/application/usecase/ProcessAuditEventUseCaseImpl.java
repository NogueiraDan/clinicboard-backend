package com.clinicboard.notification_service.audit.application.usecase;

import com.clinicboard.notification_service.audit.application.port.in.ProcessAuditEventUseCase;
import com.clinicboard.notification_service.audit.application.port.out.AuditLogPersistencePort;
import com.clinicboard.notification_service.audit.domain.event.AppointmentAuditEvent;
import com.clinicboard.notification_service.audit.domain.model.AuditLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementação do caso de uso de processamento de eventos de auditoria.
 * 
 * Responsabilidades:
 * - Receber eventos de auditoria do Kafka
 * - Converter eventos em entidades de domínio
 * - Persistir logs de auditoria de forma imutável
 * - Garantir idempotência (mesmo evento não duplica registro)
 * 
 * Segue o padrão Hexagonal Architecture onde casos de uso
 * orquestram a lógica de negócio sem conhecer detalhes de infraestrutura.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessAuditEventUseCaseImpl implements ProcessAuditEventUseCase {
    
    private final AuditLogPersistencePort auditLogPersistencePort;
    
    /**
     * Processa evento de auditoria recebido do Kafka.
     * 
     * Garante idempotência verificando se evento já foi processado.
     * Eventos são imutáveis - uma vez salvos, nunca são alterados.
     */
    @Override
    @Transactional
    public void processEvent(AppointmentAuditEvent event) {
        log.info("🔍 Processing audit event: {} - {} - {}", 
            event.getEventType(), 
            event.getAggregateId(),
            event.getEventId());
        
        // ✅ Idempotência: Verifica se evento já foi processado
        if (auditLogPersistencePort.findByEventId(event.getEventId()).isPresent()) {
            log.warn("⚠️ Event already processed (idempotent): {}", event.getEventId());
            return;
        }
        
        // ✅ Converte evento em entidade de domínio
        AuditLog auditLog = AuditLog.fromEvent(
            event.getEventId(),
            event.getAggregateId(),
            event.getEventType(),
            event.getProfessionalId(),
            event.getPatientId(),
            event.getDate(),
            event.getHour(),
            event.getOccurredAt(),
            event.getChangedBy(),
            event.getMetadata()
        );
        
        // ✅ Persiste de forma imutável
        AuditLog savedLog = auditLogPersistencePort.save(auditLog);
        
        log.info("✅ Audit log saved: {} - {} - {}", 
            savedLog.getEventType(),
            savedLog.getAggregateId(),
            savedLog.getEventId());
    }
}