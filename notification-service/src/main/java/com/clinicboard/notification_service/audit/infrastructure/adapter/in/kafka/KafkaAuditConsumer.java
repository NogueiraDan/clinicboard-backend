package com.clinicboard.notification_service.audit.infrastructure.adapter.in.kafka;

import com.clinicboard.notification_service.audit.application.port.in.ProcessAuditEventUseCase;
import com.clinicboard.notification_service.audit.domain.event.AppointmentAuditEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada que consome eventos de auditoria do Kafka.
 * 
 * Responsabilidades:
 * - Escutar tópico de auditoria no Kafka
 * - Desserializar eventos recebidos
 * - Delegar processamento para caso de uso
 * - Tratar erros e logging
 * 
 * Segue o padrão Hexagonal Architecture onde adaptadores
 * de entrada chamam portas de entrada da camada de aplicação.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaAuditConsumer {
    
    private final ProcessAuditEventUseCase processAuditEventUseCase;
    
    /**
     * Consome eventos de auditoria do Kafka.
     * 
     * Configuração:
     * - Topic: clinicboard.appointments.audit
     * - Group ID: audit-consumer-group
     * - Offset: earliest (processa eventos desde o início)
     * 
     * Idempotência: Eventos duplicados são ignorados no caso de uso
     */
    @KafkaListener(
        topics = "${app.kafka.topic.appointment-audit}",
        groupId = "audit-consumer-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeAuditEvent(
            @Payload AppointmentAuditEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        log.info("📥 Received audit event from Kafka - Partition: {} | Offset: {} | Event: {} - {}", 
            partition,
            offset,
            event.getEventType(), 
            event.getAggregateId());
        
        try {
            processAuditEventUseCase.processEvent(event);
            
            log.info("✅ Audit event processed successfully: {} - Partition: {} | Offset: {}", 
                event.getEventId(),
                partition,
                offset);
                
        } catch (Exception e) {
            log.error("❌ Failed to process audit event: {} - Partition: {} | Offset: {} - Error: {}", 
                event.getEventId(),
                partition,
                offset,
                e.getMessage(), 
                e);
            
            // Kafka commitará o offset mesmo com erro
            // Para retry, configure Dead Letter Topic
            throw e; // Re-throw para Kafka saber que falhou
        }
    }
}